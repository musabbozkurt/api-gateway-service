package com.mb.apigateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Creates the Elasticsearch intermediate CA bundle ({@code es-ca.crt}) once at startup.
 * <p>
 * Connects to the Elasticsearch HTTPS endpoint only to read the certificate chain (nothing is sent), collects
 * the non-leaf certificates it presents and, when the intermediate is omitted, downloads it from the leaf's
 * Authority Information Access "CA Issuers" URL. The completed chain is validated against the JDK default trust
 * anchors before the result is written as PEM to {@link #bundlePath()}.
 * If the file already exists, nothing is done.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticsearchCaBundleGenerator {

    public static final String BUNDLE_PATH_PROPERTY = "user-activity.elasticsearch.ca-bundle-path";

    private static final String AIA_OID_EXTENSION = "1.3.6.1.5.5.7.1.1";
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\x21-\\x7e]+");
    private static final int TIMEOUT_MS = 5000;

    public static Path bundlePath() {
        String configured = System.getProperty(BUNDLE_PATH_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "es-ca.crt");
    }

    /**
     * Generates the bundle if it does not exist yet. Never throws: on failure the application falls back to the
     * bundled classpath copy.
     */
    public static synchronized void generateIfMissing(String baseUrl) {
        try {
            Path target = bundlePath();
            if (Files.isRegularFile(target) && Files.size(target) > 0) {
                log.info("Elasticsearch CA bundle already present at {}", target);
                return;
            }
            if (baseUrl == null || !"https".equalsIgnoreCase(URI.create(baseUrl).getScheme())) {
                return;
            }
            URI uri = URI.create(baseUrl);
            int port = uri.getPort() > 0 ? uri.getPort() : 443;

            List<X509Certificate> chain = fetchChain(uri.getHost(), port);
            Map<String, X509Certificate> bundle = new LinkedHashMap<>();
            for (int i = 1; i < chain.size(); i++) {
                X509Certificate cert = chain.get(i);
                if (!isSelfSigned(cert)) {
                    bundle.put(cert.getSubjectX500Principal().getName(), cert);
                }
            }
            if (bundle.isEmpty()) {
                X509Certificate issuer = downloadIssuer(chain.getFirst());
                if (issuer != null) {
                    bundle.put(issuer.getSubjectX500Principal().getName(), issuer);
                }
            }
            if (bundle.isEmpty()) {
                log.warn("No intermediate CA found for {}:{}; using bundled classpath es-ca.crt", uri.getHost(), port);
                return;
            }

            validateChain(chain.getFirst(), bundle.values());

            StringBuilder pem = new StringBuilder();
            for (X509Certificate cert : bundle.values()) {
                pem.append("-----BEGIN CERTIFICATE-----\n")
                        .append(Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(cert.getEncoded()))
                        .append("\n-----END CERTIFICATE-----\n");
            }
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(target, pem.toString(), StandardCharsets.US_ASCII);
            log.info("Generated Elasticsearch CA bundle with {} certificate(s) at {}", bundle.size(), target);
        } catch (Exception e) {
            log.warn("Could not generate Elasticsearch CA bundle, using bundled classpath es-ca.crt: {}", e.toString());
        }
    }

    /**
     * Server validation stays enabled: every chain is checked by the JDK default trust manager. The chain is
     * captured before validation, so when Elasticsearch omits the intermediate (handshake fails with PKIX path
     * building failed) the leaf is still known; the completed chain is validated in {@link #validateChain}.
     */
    private static List<X509Certificate> fetchChain(String host, int port) {
        AtomicReference<List<X509Certificate>> captured = new AtomicReference<>(List.of());
        try {
            X509TrustManager delegate = defaultTrustManager();
            TrustManager[] capturing = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    delegate.checkClientTrusted(chain, authType);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    captured.set(List.of(chain));
                    delegate.checkServerTrusted(chain, authType);
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return delegate.getAcceptedIssuers();
                }
            }};
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, capturing, null);
            handshake(context, host, port);
        } catch (IOException | GeneralSecurityException e) {
            log.debug("TLS handshake with {}:{} did not validate on its own: {}", host, port, e.toString());
        }
        if (captured.get().isEmpty()) {
            throw new IllegalStateException("Failed to fetch certificate chain from " + host + ":" + port);
        }
        return captured.get();
    }

    private static void handshake(SSLContext context, String host, int port) throws IOException {
        try (SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket()) {
            SSLParameters parameters = socket.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            socket.setSSLParameters(parameters);
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            socket.startHandshake();
        }
    }

    private static X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        return Arrays.stream(factory.getTrustManagers())
                .filter(X509TrustManager.class::isInstance)
                .map(X509TrustManager.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default X509TrustManager available"));
    }

    /**
     * Validates leaf + intermediates against the JDK default trust anchors before the bundle is persisted.
     */
    private static void validateChain(X509Certificate leaf, Collection<X509Certificate> intermediates) throws GeneralSecurityException {
        Set<TrustAnchor> anchors = Arrays.stream(defaultTrustManager().getAcceptedIssuers())
                .map(cert -> new TrustAnchor(cert, null))
                .collect(Collectors.toSet());
        List<X509Certificate> path = new ArrayList<>();
        path.add(leaf);
        path.addAll(intermediates);
        PKIXParameters params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);
        CertPathValidator.getInstance("PKIX").validate(CertificateFactory.getInstance("X.509").generateCertPath(path), params);
    }

    private static X509Certificate downloadIssuer(X509Certificate leaf) {
        byte[] extension = leaf.getExtensionValue(AIA_OID_EXTENSION);
        if (extension == null) {
            return null;
        }
        Matcher matcher = URL_PATTERN.matcher(new String(extension, StandardCharsets.ISO_8859_1));
        while (matcher.find()) {
            String url = matcher.group();
            if (url.toLowerCase().contains("ocsp")) {
                continue;
            }
            try {
                var connection = URI.create(url).toURL().openConnection();
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                try (InputStream in = connection.getInputStream()) {
                    X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
                    if (cert.getSubjectX500Principal().equals(leaf.getIssuerX500Principal())) {
                        return cert;
                    }
                }
            } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
                log.debug("Issuer download from {} failed: {}", url, e.toString());
            }
        }
        return null;
    }

    private static boolean isSelfSigned(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }
}
