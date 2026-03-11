package com.mb.springconfigserver.watcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.PathDestinationFactory;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Polls {@code config_server_schema.properties} at a fixed interval and automatically
 * broadcasts a {@link RefreshRemoteApplicationEvent} to <b>all</b> connected services
 * via Spring Cloud Bus (Kafka) whenever an INSERT, UPDATE, or DELETE is detected —
 * with no manual API call required.
 *
 * <h3>Flow</h3>
 * <pre>
 *  DB row changed (INSERT / UPDATE / DELETE)
 *       │
 *       ▼  (next poll cycle — default every 5 s)
 *  ConfigChangeWatcher#watchForChanges()
 *       │  checksum differs from last snapshot
 *       ▼
 *  ApplicationEventPublisher.publishEvent(RefreshRemoteApplicationEvent, destination="**")
 *       │
 *       ▼  (Spring Cloud Bus serializes and sends to Kafka topic "springCloudBus")
 *  ALL services with spring-cloud-starter-bus-kafka receive the event
 *       │
 *       ▼
 *  Each service re-fetches config from Config Server
 *  → @RefreshScope beans are destroyed and recreated with fresh @Value fields
 *  → @ConfigurationProperties beans are rebound by ConfigurationPropertiesRebinder
 * </pre>
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * config:
 *   change-watcher:
 *     poll-interval-ms: 5000  # polling frequency in milliseconds (default: 5000)
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigChangeWatcher {

    /**
     * Ordered query ensures a stable row ordering so that the same data always
     * produces the same hash, regardless of the DB's physical storage order.
     */
    private static final String QUERY =
            """
                    SELECT APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE
                    FROM config_server_schema.properties
                    ORDER BY APPLICATION, PROFILE, LABEL, PROP_KEY
                    """;

    /**
     * Spring JDBC template — reuses the datasource already configured for the config server.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Spring's event bus — publishing a RemoteApplicationEvent here causes Cloud Bus to forward it to Kafka.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Provides this service's bus identity (origin service ID used in the event).
     */
    private final BusProperties busProperties;

    /**
     * Checksum of the last observed state of the properties table.
     * <p>{@code 0} is the sentinel "not yet initialized" value.
     * Using {@code volatile} ensures visibility across threads (scheduler thread-pool).
     */
    private volatile int lastChecksum = 0;

    /**
     * Polls the properties table and publishes a {@link RefreshRemoteApplicationEvent}
     * to all connected services if any change is detected since the last poll.
     *
     * <p>The very first invocation only establishes the baseline checksum — it does
     * <em>not</em> trigger a refresh, avoiding a spurious reload on startup.
     *
     * <p>Configured via {@code config.change-watcher.poll-interval-ms} (default: 5 000 ms).
     * Uses {@code fixedDelay} (not {@code fixedRate}) so that the next poll only starts
     * after the current one fully completes, preventing overlapping executions.
     */
    @Scheduled(fixedDelayString = "${config.change-watcher.poll-interval-ms:5000}")
    public void watchForChanges() {
        log.info("ConfigChangeWatcher — polling for config changes...");
        try {
            // Fetch current snapshot of all config properties, ordered for a stable hash
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(QUERY);

            // Compute a lightweight checksum — List.hashCode() aggregates each row Map's hashCode
            int currentChecksum = rows.hashCode();

            if (lastChecksum == 0) {
                // First poll: record baseline without triggering a refresh
                log.info("ConfigChangeWatcher initialised — baseline checksum: {}", currentChecksum);
                lastChecksum = currentChecksum;
                return;
            }

            if (currentChecksum != lastChecksum) {
                log.info("ConfigChangeWatcher — DB change detected (checksum {} → {}). Broadcasting refresh to all services via Spring Cloud Bus (Kafka).", lastChecksum, currentChecksum);

                // Update the snapshot BEFORE publishing to avoid re-triggering on slow Kafka round-trips
                lastChecksum = currentChecksum;

                // "**" is an Ant-style wildcard destination that matches ALL connected service instances
                // subscribed to the springCloudBus Kafka topic. Spring Cloud Bus 2025.1.0 does not
                // expose a DESTINATION_ALL constant — use the literal value directly.
                eventPublisher.publishEvent(new RefreshRemoteApplicationEvent(this, busProperties.getId(), (new PathDestinationFactory()).getDestination("**")));
            }
        } catch (Exception e) {
            log.error("ConfigChangeWatcher — unexpected error while polling for config changes. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
