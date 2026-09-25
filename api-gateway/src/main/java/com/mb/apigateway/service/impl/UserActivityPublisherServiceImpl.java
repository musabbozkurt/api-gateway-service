package com.mb.apigateway.service.impl;

import com.mb.apigateway.config.UserActivityElasticsearchProperties;
import com.mb.apigateway.logging.UserActivityEvent;
import com.mb.apigateway.service.UserActivityPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mb.apigateway.constant.GatewayServiceConstants.*;

/**
 * Unified component for building and publishing user activity events to Elasticsearch asynchronously.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li><b>Non-blocking</b> - Uses reactive WebClient; never blocks gateway request threads.</li>
 *   <li><b>Best-effort delivery</b> - Elasticsearch failures never affect requests; they are logged at DEBUG.</li>
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
 *      base-url: http://localhost:9200
 *      username: elastic          # optional, basic auth
 *      password: <password>       # optional, basic auth
 *      index-prefix: user-activity
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
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

    private final boolean enabled;
    private final String indexPrefix;
    private final WebClient webClient;
    private final Map<String, Mono<Void>> indexInitializers = new ConcurrentHashMap<>();

    public UserActivityPublisherServiceImpl(WebClient.Builder webClientBuilder, UserActivityElasticsearchProperties properties) {
        this.enabled = properties.isEnabled() && StringUtils.hasText(properties.getBaseUrl());
        this.indexPrefix = properties.getIndexPrefix();

        // Clone so the shared builder is never mutated with ES-specific settings/credentials
        WebClient.Builder builder = webClientBuilder.clone()
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
                .timeout(REQUEST_TIMEOUT)
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
                .timeout(REQUEST_TIMEOUT)
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
                .timeout(REQUEST_TIMEOUT);
    }

    /**
     * Indexes a single document, adding {@code @timestamp} if missing.
     * The timestamp is used for sorting and Kibana time filtering.
     */
    private Mono<Void> writeDocument(String indexName, Map<String, Object> payload, Instant timestamp) {
        Map<String, Object> document = new HashMap<>(payload);
        document.putIfAbsent(TIMESTAMP_FIELD, timestamp.toString());

        return webClient.post()
                .uri("/{index}/_doc", indexName)
                .bodyValue(document)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .timeout(REQUEST_TIMEOUT)
                .then();
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
