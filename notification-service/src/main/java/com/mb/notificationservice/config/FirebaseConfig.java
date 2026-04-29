package com.mb.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseConfig implements ResourceLoaderAware {

    /**
     * Map of Firebase applications. Key is the applicationName, value is the service account credential source.
     *
     * <p>The value is resolved in 3 ways based on its content:</p>
     * <ul>
     *     <li><b>Spring Resource</b> (prefix {@code classpath:} or {@code file:} or {@code https:}):
     *         loaded via Spring's {@link ResourceLoader}.</li>
     *     <li><b>Inline JSON</b> (value starts with {@code {}):
     *         treated as raw JSON content.</li>
     *     <li><b>File path</b> (anything else):
     *         treated as a file system path.</li>
     * </ul>
     * <p>
     * Example:
     * <pre>
     * firebase.apps.first-app-application=classpath:firebase/first-app-service-account.json
     * firebase.apps.second-app-application={"type":"service_account","project_id":"...","private_key":"..."}
     * firebase.apps.another-application=/etc/config/another-service-account.json
     * </pre>
     */
    private Map<String, String> apps = new HashMap<>();

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        apps.forEach((application, serviceAccountValue) -> {
            if (FirebaseApp.getApps().stream().noneMatch(app -> app.getName().equals(application))) {
                try (InputStream serviceAccount = resolveInputStream(serviceAccountValue)) {
                    FirebaseApp.initializeApp(
                            FirebaseOptions.builder()
                                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                    .build(),
                            application
                    );
                    log.info("Firebase app initialized successfully for application: {}", application);
                } catch (Exception e) {
                    log.error("Exception occurred while initializing Firebase for application: {}. Exception: {}", application, ExceptionUtils.getMessage(e));
                }
            }
        });
    }

    /**
     * @param application the applicationName used during Firebase initialization
     * @return the {@link FirebaseApp} instance associated with the given applicationName
     */
    public FirebaseApp getFirebaseApp(String application) {
        return FirebaseApp.getInstance(application);
    }

    private InputStream resolveInputStream(String value) throws IOException {
        String trimmed = value.trim();
        if (trimmed.startsWith("classpath:") || trimmed.startsWith("file:") || trimmed.startsWith("https:")) {
            return resourceLoader.getResource(trimmed).getInputStream();
        } else if (trimmed.startsWith("{")) {
            return new ByteArrayInputStream(trimmed.getBytes(StandardCharsets.UTF_8));
        } else {
            return Files.newInputStream(Paths.get(trimmed));
        }
    }
}
