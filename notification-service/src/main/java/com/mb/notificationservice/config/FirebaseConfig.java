package com.mb.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseConfig {

    /**
     * Map of Firebase applications. Key is the applicationName, value is the service account JSON resource location.
     * Example:
     * <pre>
     * firebase.apps.my-application=classpath:firebase/my-service-account.json
     * firebase.apps.another-application=file:/etc/config/another-service-account.json
     * </pre>
     */
    private Map<String, Resource> apps = new HashMap<>();

    @PostConstruct
    public void initialize() {
        apps.forEach((application, serviceAccountResource) -> {
            if (FirebaseApp.getApps().stream().noneMatch(app -> app.getName().equals(application))) {
                try (InputStream serviceAccount = serviceAccountResource.getInputStream()) {
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
}
