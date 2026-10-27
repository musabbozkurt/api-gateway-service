package com.mb.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "user-activity.elasticsearch")
public class UserActivityElasticsearchProperties {

    private String baseUrl = "http://localhost:9200";
    private String username;
    private String password;
    private boolean enabled = true;
    private String indexPrefix = "user-activity";

    /**
     * Applies to {@code https} base URLs only. When true, the certificate is validated against the JDK CAs plus
     * {@code classpath:elasticsearch/es-ca.crt} with hostname verification. When false, trust all TLS certificates
     * and skip hostname verification (Logstash {@code ssl_certificate_verified}); not recommended for production.
     */
    private boolean sslCertificateVerified = true;

    /**
     * Max pooled connections to Elasticsearch (dedicated pool, not shared with gateway routing).
     */
    private int maxConnections = 5;

    /**
     * Max requests waiting for a free pooled connection; excess events are dropped with a warning.
     */
    private int pendingAcquireMaxCount = 100;

    /**
     * Idle connections are closed after this time. Keep it below the proxy/LB/ES idle timeout.
     */
    private Duration maxIdleTime = Duration.ofSeconds(20);

    /**
     * Connections are recycled after this lifetime regardless of activity.
     */
    private Duration maxLifeTime = Duration.ofMinutes(5);

    /**
     * Interval of the background eviction of idle/expired connections.
     */
    private Duration evictInterval = Duration.ofSeconds(30);
    private Duration connectTimeout = Duration.ofSeconds(2);

    /**
     * Timeout applied to every single HTTP attempt.
     */
    private Duration requestTimeout = Duration.ofSeconds(2);

    /**
     * Retries for connection errors and 429/502/503/504 (0 disables retry).
     */
    private int retryMaxAttempts = 3;
    private Duration retryMinBackoff = Duration.ofMillis(200);
    private Duration retryMaxBackoff = Duration.ofSeconds(2);
}
