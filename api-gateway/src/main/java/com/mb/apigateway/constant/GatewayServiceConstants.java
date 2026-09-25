package com.mb.apigateway.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GatewayServiceConstants {

    public static final String USERNAME = "username";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "user_name";
    public static final String CLIENT_ID = "client_id";
    public static final String SESSION_ID = "sessionId";
    public static final String MDC_CONTEXT = "MDC_CONTEXT";
    public static final String API = "api";
    public static final String DEVICE_INFO_HEADER = "deviceInfo";
    public static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    public static final String PAGE_URL_HEADER = "X-Page-Url";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String USER_ACTIVITY_LOGGER = "USER_ACTIVITY";
    public static final String USER_ACTIVITY_EVENT_TYPE = "USER_ACTIVITY_EVENT";

    public static final String RESPONSE_BODY_CONTAINS_ANY_ERROR = "responseBodyContainsAnyError";
    public static final String SERVICE_ACCESS_KEY_PREFIX = "swagger-application:service-access:";

    // User Activity Event Fields
    public static final String EVENT_TYPE = "eventType";
    public static final String REQUEST_ID = "requestId";
    public static final String SERVICE_NAME = "serviceName";
    public static final String METHOD = "method";
    public static final String STATUS = "status";
    public static final String STARTED_AT = "startedAt";
    public static final String FINISHED_AT = "finishedAt";
    public static final String DURATION_MS = "durationMs";
    public static final String HTTP_STATUS = "httpStatus";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String TIMESTAMP_FIELD = "@timestamp";
    public static final String INDEX_ALREADY_EXISTS = "resource_already_exists_exception";

    // Elasticsearch mapping type definitions
    private static final Map<String, String> TYPE_KEYWORD = Map.of("type", "keyword");
    private static final Map<String, String> TYPE_TEXT = Map.of("type", "text");
    private static final Map<String, String> TYPE_DATE = Map.of("type", "date");
    private static final Map<String, String> TYPE_LONG = Map.of("type", "long");
    private static final Map<String, String> TYPE_INTEGER = Map.of("type", "integer");

    /**
     * Immutable request body used to create monthly user activity indices (settings + explicit mappings).
     */
    public static final Map<String, Object> USER_ACTIVITY_INDEX_DEFINITION =
            Map.of(
                    "settings", Map.of("number_of_shards", 1, "number_of_replicas", 1),
                    "mappings", Map.of(
                            "properties", Map.ofEntries(
                                    Map.entry(TIMESTAMP_FIELD, TYPE_DATE),
                                    Map.entry(EVENT_TYPE, TYPE_KEYWORD),
                                    Map.entry(STATUS, TYPE_KEYWORD),
                                    Map.entry(API, TYPE_KEYWORD),
                                    Map.entry(METHOD, TYPE_KEYWORD),
                                    Map.entry(SERVICE_NAME, TYPE_KEYWORD),
                                    Map.entry(REQUEST_ID, TYPE_KEYWORD),
                                    Map.entry(CLIENT_ID, TYPE_KEYWORD),
                                    Map.entry(USER_ID, TYPE_KEYWORD),
                                    Map.entry(USERNAME, TYPE_KEYWORD),
                                    Map.entry(IP_ADDRESS, TYPE_KEYWORD),
                                    Map.entry(DEVICE_INFO_HEADER, TYPE_TEXT),
                                    Map.entry(ERROR_MESSAGE, TYPE_TEXT),
                                    Map.entry(STARTED_AT, TYPE_DATE),
                                    Map.entry(FINISHED_AT, TYPE_DATE),
                                    Map.entry(DURATION_MS, TYPE_LONG),
                                    Map.entry(HTTP_STATUS, TYPE_INTEGER)
                            )
                    )
            );
}
