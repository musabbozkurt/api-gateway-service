package com.mb.apigateway.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

import static com.mb.apigateway.constant.GatewayServiceConstants.*;

@Component
public class UserActivityLogger {

    private static final Logger ACTIVITY_LOG = LoggerFactory.getLogger(USER_ACTIVITY_LOGGER);

    public void log(ServerWebExchange exchange, UserActivityEvent activityEvent) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            putIfPresent(CLIENT_ID, exchange.getAttributes().get(CLIENT_ID));
            putIfPresent(USER_ID, exchange.getAttributes().get(USER_ID));
            putIfPresent(USERNAME, exchange.getAttributes().get(USERNAME));
            putIfPresent(API, activityEvent.api());
            putIfPresent(PAGE_URL_HEADER, exchange.getRequest().getHeaders().getFirst(PAGE_URL_HEADER));
            putIfPresent(DEVICE_INFO_HEADER, exchange.getRequest().getHeaders().getFirst(USER_AGENT_HEADER));

            putIfPresent("eventType", USER_ACTIVITY_EVENT_TYPE);
            putIfPresent("requestId", activityEvent.requestId());
            putIfPresent("serviceName", activityEvent.serviceName());
            putIfPresent("method", activityEvent.method());
            putIfPresent("status", activityEvent.status().name());
            putIfPresent("startedAt", activityEvent.startedAt());
            putIfPresent("finishedAt", activityEvent.finishedAt());
            putIfPresent("durationMs", activityEvent.durationMs());
            putIfPresent("httpStatus", activityEvent.httpStatus());
            putIfPresent("errorMessage", activityEvent.errorMessage());
            putIfPresent("ipAddress", extractClientIp(exchange));

            ACTIVITY_LOG.info(USER_ACTIVITY_EVENT_TYPE);
        } finally {
            if (previousContext != null) {
                MDC.setContextMap(previousContext);
            } else {
                MDC.clear();
            }
        }
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst(FORWARDED_FOR_HEADER);
        if (StringUtils.isNotBlank(forwardedFor)) {
            String[] parts = forwardedFor.split(",");
            return parts[0].trim();
        }

        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return null;
    }

    private void putIfPresent(String key, Object value) {
        if (value == null) {
            return;
        }
        String stringValue = String.valueOf(value);
        if (StringUtils.isNotBlank(stringValue)) {
            MDC.put(key, stringValue);
        }
    }
}
