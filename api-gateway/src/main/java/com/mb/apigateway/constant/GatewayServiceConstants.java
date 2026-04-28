package com.mb.apigateway.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GatewayServiceConstants {

    public static final String USERNAME = "username";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "user_name";
    public static final String CLIENT_ID = "client_id";
    public static final String SESSION_ID = "sessionId";
    public static final String MDC_CONTEXT = "MDC_CONTEXT";

    public static final String RESPONSE_BODY_CONTAINS_ANY_ERROR = "responseBodyContainsAnyError";
}
