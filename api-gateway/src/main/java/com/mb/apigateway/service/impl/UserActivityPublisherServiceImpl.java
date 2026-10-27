package com.mb.apigateway.service.impl;

import com.mb.apigateway.config.UserActivityElasticsearchProperties;
import com.mb.apigateway.logging.UserActivityEvent;
import com.mb.apigateway.service.UserActivityPublisherService;
import com.mb.apigateway.utils.ElasticsearchCaBundleGenerator;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.internal.shaded.reactor.pool.PoolAcquirePendingLimitException;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.mb.apigateway.constant.GatewayServiceConstants.*;

/**
 * Unified component for building and publishing user activity events to Elasticsearch asynchronously.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li><b>Non-blocking</b> - Uses reactive WebClient; never blocks gateway request threads.</li>
 *   <li><b>Best-effort delivery</b> - Elasticsearch failures never affect requests; they are logged at WARN.</li>
 *   <li><b>Monthly indices</b> - Auto-creates indices like {@code user-activity-2026-09} (UTC).</li>
 *   <li><b>Timestamp auto-population</b> - Every document includes {@code @timestamp} for sorting and Kibana.</li>
 * </ul>
 * <p>
 * <b>Configuration:</b>
 * <p>
 * Configuration is loaded via {@link UserActivityElasticsearchProperties} from:
 * <ul>
 *   <li>application.yml (local development)</li>
 *   <li>Spring Cloud Vault (production)</li>
 * </ul>
 * <p>
 * Example Vault path structure:
 * <pre>{@code
 *  user-activity:
 *    elasticsearch:
 *      enabled: true
 *      base-url: https://<es-host>.<your-domain>:9200   # https + host name from the certificate, not the IP
 *      username: elastic          # optional, basic auth
 *      password: <password>       # optional, basic auth
 *      index-prefix: user-activity
 *      ssl-certificate-verified: true  # false = trust-all (Logstash-compatible); see docs/ELASTICSEARCH_TLS_FIX.md
 *      # optional tuning (defaults shown)
 *      max-connections: 5
 *      pending-acquire-max-count: 100
 *      max-idle-time: 20s          # keep below the proxy/LB/ES idle timeout
 *      max-life-time: 5m
 *      evict-interval: 30s
 *      connect-timeout: 2s
 *      request-timeout: 2s         # per HTTP attempt
 *      retry-max-attempts: 3       # connection errors and 429/502/503/504 only
 *      retry-min-backoff: 200ms
 *      retry-max-backoff: 2s
 * }</pre>
 * <p>
 * <b>Query Examples</b> (add {@code -u <username>:<password>} when security is enabled):
 * <pre>{@code
 *
 * # 1. List all user activity indices
 * curl -s "http://localhost:9200/_cat/indices/user-activity-*?v&s=index"
 *
 * # 2. Show the mapping of the current month index
 * curl -s "http://localhost:9200/user-activity-2026-09/_mapping?pretty"
 *
 * # 3. Latest 20 events (newest first)
 * curl -s -X GET "http://localhost:9200/user-activity-2026-09/_search?pretty" \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *         "size": 20,
 *         "query": { "term": { "eventType": "USER_API_ACTIVITY" } },
 *         "sort":  [ { "@timestamp": { "order": "desc" } } ]
 *       }'
 *
 * # 4. Failed events of a user in the last 24 hours (all months)
 * ALL_INDICES='user-activity-*'
 * curl -s -X GET "http://localhost:9200/$ALL_INDICES/_search?pretty" \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *         "query": {
 *           "bool": {
 *             "filter": [
 *               { "term":  { "userId": "user-1" } },
 *               { "term":  { "status": "FAILED" } },
 *               { "range": { "@timestamp": { "gte": "now-24h" } } }
 *             ]
 *           }
 *         },
 *         "sort": [ { "@timestamp": { "order": "desc" } } ]
 *       }'
 *
 * # 5. Delete an index to force recreation with updated mappings (irreversible!)
 * curl -s -X DELETE "http://localhost:9200/user-activity-2026-09"
 * }</pre>
 */
@Slf4j
@Service
public class UserActivityPublisherServiceImpl implements UserActivityPublisherService {

    private static final DateTimeFormatter INDEX_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);
    private static final String ES_CA_CERTIFICATES = "elasticsearch/es-ca.crt";
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            HttpStatus.BAD_GATEWAY.value(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            HttpStatus.GATEWAY_TIMEOUT.value()
    );

    private final boolean enabled;
    private final String indexPrefix;
    private final WebClient webClient;
    private final Duration requestTimeout;
    private final Retry retrySpec;
    private final Map<String, Mono<Void>> indexInitializers = new ConcurrentHashMap<>();

    public UserActivityPublisherServiceImpl(WebClient.Builder webClientBuilder, UserActivityElasticsearchProperties properties) {
        this.enabled = properties.isEnabled() && StringUtils.hasText(properties.getBaseUrl());
        this.indexPrefix = properties.getIndexPrefix();
        this.requestTimeout = properties.getRequestTimeout();
        this.retrySpec = buildRetrySpec(properties);

        // Clone so the shared builder is never mutated with ES-specific settings/credentials
        WebClient.Builder builder = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient(properties)))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(properties.getBaseUrl())) {
            builder.baseUrl(properties.getBaseUrl());
        }

        if (StringUtils.hasText(properties.getUsername()) && StringUtils.hasText(properties.getPassword())) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(properties.getUsername(), properties.getPassword()));
        }

        this.webClient = builder.build();
    }

    /**
     * Dedicated pool for Elasticsearch. Idle/lifetime limits plus background eviction keep connections that were
     * already closed by Elasticsearch or an intermediary from being reused; retries cover the remaining race.
     * <p>
     * For an {@code https} base URL, TLS depends on {@link UserActivityElasticsearchProperties#isSslCertificateVerified()}:
     * <ul>
     *   <li>{@code true} - JDK CAs plus the bundled intermediates, hostname verification on (see {@link #buildSslContext()})</li>
     *   <li>{@code false} - trust-all, certificate and hostname checks skipped for this client only</li>
     * </ul>
     * An {@code http} base URL is always sent as plain HTTP.
     */
    private static HttpClient buildHttpClient(UserActivityElasticsearchProperties properties) {
        ConnectionProvider provider = ConnectionProvider.builder("user-activity-es")
                .maxConnections(properties.getMaxConnections())
                .pendingAcquireMaxCount(properties.getPendingAcquireMaxCount())
                .pendingAcquireTimeout(properties.getRequestTimeout())
                .maxIdleTime(properties.getMaxIdleTime())
                .maxLifeTime(properties.getMaxLifeTime())
                .evictInBackground(properties.getEvictInterval())
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis());

        if (!isHttps(properties.getBaseUrl())) {
            return httpClient;
        }

        if (!properties.isSslCertificateVerified()) {
            try {
                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                httpClient = httpClient.secure(spec -> spec
                        .sslContext(sslContext)
                        .handlerConfigurator(handler -> {
                            SSLEngine engine = handler.engine();
                            SSLParameters params = engine.getSSLParameters();
                            params.setEndpointIdentificationAlgorithm(null);
                            engine.setSSLParameters(params);
                        }));
            } catch (SSLException e) {
                throw new IllegalStateException("Failed to configure insecure SSL for user-activity Elasticsearch", e);
            }
            return httpClient;
        }

        return httpClient.secure(spec -> spec.sslContext(buildSslContext()));
    }

    private static boolean isHttps(String baseUrl) {
        return StringUtils.hasText(baseUrl) && "https".equalsIgnoreCase(URI.create(baseUrl).getScheme());
    }

    /**
     * Trusts the JDK default CAs plus bundled intermediate CAs in {@value #ES_CA_CERTIFICATES}.
     * Elasticsearch often sends only its own certificate without the intermediate, so the JDK alone cannot build
     * the chain ({@code PKIX path building failed}). Hostname verification stays enabled.
     * <p>
     * Built once at startup; a missing or invalid bundle fails the startup instead of every event.
     */
    private static SslContext buildSslContext() {
        Path generated = ElasticsearchCaBundleGenerator.bundlePath();
        try (InputStream bundle = Files.isRegularFile(generated) ? Files.newInputStream(generated) : new ClassPathResource(ES_CA_CERTIFICATES).getInputStream()) {
            List<X509Certificate> trusted = new ArrayList<>(Arrays.asList(defaultTrustManager().getAcceptedIssuers()));
            CertificateFactory.getInstance("X.509").generateCertificates(bundle)
                    .forEach(certificate -> trusted.add((X509Certificate) certificate));

            return SslContextBuilder.forClient()
                    .trustManager(trusted.toArray(X509Certificate[]::new))
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Elasticsearch CA certificates could not be loaded from classpath:" + ES_CA_CERTIFICATES, e);
        }
    }

    /**
     * Returns the JDK default trust manager (cacerts or {@code javax.net.ssl.trustStore} if set).
     */
    private static X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        for (TrustManager trustManager : factory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager x509TrustManager) {
                return x509TrustManager;
            }
        }
        throw new IllegalStateException("No default X509TrustManager available");
    }

    /**
     * Retries only transient failures: connection-level errors (e.g. a stale pooled connection) and
     * 429/502/503/504 responses. Timeouts, pool exhaustion and other 4xx/5xx are not retried.
     */
    private static Retry buildRetrySpec(UserActivityElasticsearchProperties properties) {
        return Retry.backoff(properties.getRetryMaxAttempts(), properties.getRetryMinBackoff())
                .maxBackoff(properties.getRetryMaxBackoff())
                .filter(UserActivityPublisherServiceImpl::isRetryable)
                .onRetryExhaustedThrow((_, signal) -> signal.failure());
    }

    private static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientRequestException) {
            // Pool exhaustion means the pool is saturated: retrying would only add load
            return !(throwable.getCause() instanceof PoolAcquirePendingLimitException);
        }
        return throwable instanceof WebClientResponseException responseException && RETRYABLE_STATUSES.contains(responseException.getStatusCode().value());
    }

    /**
     * Publishes a user activity event to Elasticsearch asynchronously.
     * <p>
     * Builds a structured payload from the exchange and activity event, then sends it to Elasticsearch
     * in a non-blocking, fire-and-forget manner. Returns immediately; never blocks the request thread.
     *
     * @param exchange      the server exchange containing request and context
     * @param activityEvent the activity event with status, duration, and HTTP response code
     */
    @Override
    public void publish(ServerWebExchange exchange, UserActivityEvent activityEvent) {
        if (!enabled || exchange == null || activityEvent == null) {
            return;
        }
        try {
            publishToElasticsearch(buildPayload(exchange, activityEvent));
        } catch (RuntimeException e) {
            // Activity logging must never break the gateway request flow
            log.warn("Failed to publish user activity event", e);
        }
    }

    private Map<String, Object> buildPayload(ServerWebExchange exchange, UserActivityEvent activityEvent) {
        Map<String, Object> payload = new HashMap<>();

        // Request context from exchange attributes
        addIfPresent(payload, CLIENT_ID, exchange.getAttributes().get(CLIENT_ID));
        addIfPresent(payload, USER_ID, exchange.getAttributes().get(USER_ID));
        addIfPresent(payload, USERNAME, exchange.getAttributes().get(USERNAME));

        // Activity metadata
        addIfPresent(payload, API, activityEvent.api());
        addIfPresent(payload, PAGE_URL_HEADER, exchange.getRequest().getHeaders().getFirst(PAGE_URL_HEADER));
        addIfPresent(payload, DEVICE_INFO_HEADER, exchange.getRequest().getHeaders().getFirst(USER_AGENT_HEADER));

        // Event specifics
        addIfPresent(payload, EVENT_TYPE, USER_ACTIVITY_EVENT_TYPE);
        addIfPresent(payload, REQUEST_ID, activityEvent.requestId());
        addIfPresent(payload, SERVICE_NAME, activityEvent.serviceName());
        addIfPresent(payload, METHOD, activityEvent.method());
        addIfPresent(payload, STATUS, activityEvent.status() != null ? activityEvent.status().name() : null);
        addIfPresent(payload, STARTED_AT, toIsoString(activityEvent.startedAt()));
        addIfPresent(payload, FINISHED_AT, toIsoString(activityEvent.finishedAt()));
        addIfPresent(payload, DURATION_MS, activityEvent.durationMs());
        addIfPresent(payload, HTTP_STATUS, activityEvent.httpStatus());
        addIfPresent(payload, ERROR_MESSAGE, activityEvent.errorMessage());
        addIfPresent(payload, IP_ADDRESS, extractClientIp(exchange));
        return payload;
    }

    /**
     * Schedules the activity event for asynchronous delivery to Elasticsearch.
     * Returns immediately and never throws. Non-blocking; gateway request continues in background.
     * <p>
     * Timeouts are applied per HTTP call (not on the whole chain) so that a slow write can never
     * cancel the shared, cached index initialization used by concurrent events.
     *
     * @param payload flat event map; ignored if empty
     */
    private void publishToElasticsearch(Map<String, Object> payload) {
        if (payload.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        String indexName = indexPrefix + "-" + INDEX_MONTH_FORMAT.format(now);

        ensureIndex(indexName)
                .then(Mono.defer(() -> writeDocument(indexName, payload, now)))
                .doOnError(throwable -> log.warn("User activity event could not be indexed to {}: {}", indexName, throwable.toString()))
                // Swallow every async failure so nothing propagates or reaches Reactor's onErrorDropped hook
                .onErrorComplete()
                .subscribe();
    }

    /**
     * Ensures the index exists and, for an already-created index, applies the current mapping once per process.
     * Once cached, later events skip the check for that month.
     */
    private Mono<Void> ensureIndex(String indexName) {
        // computeIfAbsent guarantees a single initializer per index even under concurrent events.
        // The cached Mono replays completion instantly, so later events make no HTTP call.
        return indexInitializers.computeIfAbsent(indexName, this::newInitializer);
    }

    /**
     * Builds the shared, cached initializer. On success it stays cached for the process lifetime;
     * on failure it is evicted so a later event retries once.
     */
    private Mono<Void> newInitializer(String indexName) {
        return createIndexIfMissing(indexName)
                .doOnError(throwable -> {
                    log.warn("User activity index initialization for {} failed, will retry on next event: {}", indexName, throwable.toString());
                    indexInitializers.remove(indexName);
                })
                .cache();
    }

    /**
     * Checks if the index exists. If it does, apply the latest mapping once. If it does not, create it.
     * Each HTTP call has its own timeout so the combined chain is not cut short on a cold connection.
     */
    private Mono<Void> createIndexIfMissing(String indexName) {
        return webClient.head()
                .uri("/{index}", indexName)
                .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                .timeout(requestTimeout)
                .retryWhen(retrySpec)
                .flatMap(status -> {
                    if (status == HttpStatus.OK.value()) {
                        return updateMapping(indexName);
                    }
                    if (status == HttpStatus.NOT_FOUND.value()) {
                        return createIndex(indexName);
                    }
                    return Mono.error(new IllegalStateException("Unexpected status " + status + " while checking index " + indexName));
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> updateMapping(String indexName) {
        Map<String, Object> mappings = new HashMap<>();
        Map<String, Object> indexMappings = (Map<String, Object>) USER_ACTIVITY_INDEX_DEFINITION.get("mappings");
        if (indexMappings != null) {
            mappings.putAll(indexMappings);
        }

        return webClient.put()
                .uri("/{index}/_mapping", indexName)
                .bodyValue(mappings)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::<Void>error);
                })
                .timeout(requestTimeout)
                // Mapping update is best-effort and must run only once per process: a rejected mapping
                // (e.g. incompatible field type change) must not evict the cache and retry on every request.
                .onErrorResume(throwable -> {
                    log.warn("User activity mapping update for {} failed; continuing with existing mapping: {}", indexName, throwable.toString());
                    return Mono.empty();
                });
    }

    /**
     * Creates the index using {@link com.mb.apigateway.constant.GatewayServiceConstants#USER_ACTIVITY_INDEX_DEFINITION}.
     * A 400 {@code resource_already_exists_exception} is tolerated to handle concurrent creation races.
     */
    private Mono<Void> createIndex(String indexName) {
        return webClient.put()
                .uri("/{index}", indexName)
                .bodyValue(USER_ACTIVITY_INDEX_DEFINITION)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    }
                    if (response.statusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                        // Tolerate concurrent creation by another instance; fail on any other bad request
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errorBody -> errorBody.contains(INDEX_ALREADY_EXISTS) ? Mono.empty() : Mono.error(new IllegalStateException("Index creation rejected: " + errorBody)));
                    }
                    return response.createException().flatMap(Mono::<Void>error);
                })
                .timeout(requestTimeout)
                // Safe to retry: if the first attempt did create the index, the retry gets
                // resource_already_exists_exception, which is tolerated above.
                .retryWhen(retrySpec);
    }

    /**
     * Indexes a single document, adding {@code @timestamp} if missing.
     * The timestamp is used for sorting and Kibana time filtering.
     * <p>
     * When a {@code requestId} exists it is used as the document {@code _id} ({@code PUT /{index}/_doc/{id}}),
     * so a retried write overwrites the same document and can never create a duplicate. Without a
     * {@code requestId} the document is posted with a generated id and is not retried.
     */
    private Mono<Void> writeDocument(String indexName, Map<String, Object> payload, Instant timestamp) {
        Map<String, Object> document = new HashMap<>(payload);
        document.putIfAbsent(TIMESTAMP_FIELD, timestamp.toString());

        Object requestId = payload.get(REQUEST_ID);
        boolean idempotent = requestId != null;

        WebClient.RequestBodySpec request = idempotent
                ? webClient.put().uri("/{index}/_doc/{id}", indexName, requestId)
                : webClient.post().uri("/{index}/_doc", indexName);

        Mono<Void> write = request
                .bodyValue(document)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::<Void>error);
                })
                .timeout(requestTimeout);

        return idempotent ? write.retryWhen(retrySpec) : write;
    }

    /**
     * Extracts client IP from X-Forwarded-For header (first hop) or remote address.
     *
     * @param exchange the server exchange
     * @return the client IP address, or null if unavailable
     */
    private String extractClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(forwardedFor)) {
            String[] parts = forwardedFor.split(",");
            return parts[0].trim();
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * Converts an Instant to ISO-8601 string format.
     *
     * @param instant the instant, may be null
     * @return ISO-8601 string, or null if instant is null
     */
    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    /**
     * Adds a key-value pair to the payload map if the value is non-null and non-blank.
     * Preserves numeric types; converts other types to strings.
     *
     * @param payload the target map
     * @param key     the field key
     * @param value   the field value
     */
    private void addIfPresent(Map<String, Object> payload, String key, Object value) {
        if (value == null) {
            return;
        }

        // Keep numeric types as-is for proper Elasticsearch mapping
        if (value instanceof Number || value instanceof Boolean) {
            payload.put(key, value);
            return;
        }

        // Convert other types to string
        String stringValue = String.valueOf(value);
        if (StringUtils.hasText(stringValue)) {
            payload.put(key, stringValue);
        }
    }
}
