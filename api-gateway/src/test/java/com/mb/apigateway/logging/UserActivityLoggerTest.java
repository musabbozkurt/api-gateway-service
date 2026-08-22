package com.mb.apigateway.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mb.apigateway.enums.ActivityStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;

import static com.mb.apigateway.constant.GatewayServiceConstants.API;
import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.DEVICE_INFO_HEADER;
import static com.mb.apigateway.constant.GatewayServiceConstants.PAGE_URL_HEADER;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ACTIVITY_EVENT_TYPE;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ACTIVITY_LOGGER;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_AGENT_HEADER;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class UserActivityLoggerTest {

    private final UserActivityLogger userActivityLogger = new UserActivityLogger();
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger activityLogger = (Logger) LoggerFactory.getLogger(USER_ACTIVITY_LOGGER);
        listAppender = new ListAppender<>();
        listAppender.start();
        activityLogger.addAppender(listAppender);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        Logger activityLogger = (Logger) LoggerFactory.getLogger(USER_ACTIVITY_LOGGER);
        activityLogger.detachAppender(listAppender);
        MDC.clear();
    }

    @Test
    void log_ShouldWriteExpectedMdcFields_WhenHeadersAndAttributesExist() {
        ServerWebExchange exchange = createExchange("1.1.1.1, 2.2.2.2", new InetSocketAddress("10.0.0.7", 8080));

        exchange.getAttributes().put(CLIENT_ID, "client-1");
        exchange.getAttributes().put(USER_ID, "user-1");
        exchange.getAttributes().put(USERNAME, "tester");

        UserActivityEvent event = new UserActivityEvent(
                ActivityStatus.COMPLETED,
                "req-1",
                "product-service",
                "/api/v1/products",
                "GET",
                Instant.parse("2026-08-22T10:00:00Z"),
                Instant.parse("2026-08-22T10:00:01Z"),
                1000L,
                200,
                null
        );

        userActivityLogger.log(exchange, event);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.getFirst();
        Map<String, String> mdc = loggingEvent.getMDCPropertyMap();

        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(USER_ACTIVITY_EVENT_TYPE);
        assertThat(mdc)
                .containsEntry(CLIENT_ID, "client-1")
                .containsEntry(USER_ID, "user-1")
                .containsEntry(USERNAME, "tester")
                .containsEntry(API, "/api/v1/products")
                .containsEntry(PAGE_URL_HEADER, "/home")
                .containsEntry(DEVICE_INFO_HEADER, "JUnit-Agent")
                .containsEntry("eventType", USER_ACTIVITY_EVENT_TYPE)
                .containsEntry("requestId", "req-1")
                .containsEntry("serviceName", "product-service")
                .containsEntry("method", "GET")
                .containsEntry("status", ActivityStatus.COMPLETED.name())
                .containsEntry("httpStatus", "200")
                .containsEntry("ipAddress", "1.1.1.1");
    }

    @Test
    void log_ShouldUseRemoteAddress_WhenForwardedHeaderMissing() {
        ServerWebExchange exchange = createExchange(null, new InetSocketAddress("10.8.0.4", 8080));
        exchange.getAttributes().put(CLIENT_ID, "client-2");

        UserActivityEvent event = new UserActivityEvent(
                ActivityStatus.STARTED,
                "req-2",
                "product-service",
                "/api/v1/products",
                "POST",
                Instant.parse("2026-08-22T10:00:00Z"),
                null,
                null,
                null,
                null
        );

        userActivityLogger.log(exchange, event);

        assertThat(listAppender.list).hasSize(1);
        Map<String, String> mdc = listAppender.list.getFirst().getMDCPropertyMap();
        assertThat(mdc).containsEntry("ipAddress", "10.8.0.4");
    }

    @Test
    void log_ShouldRestoreExistingMdcContext_AfterLogging() {
        MDC.put("existingKey", "existingValue");

        ServerWebExchange exchange = createExchange("3.3.3.3", new InetSocketAddress("10.9.0.5", 8080));
        exchange.getAttributes().put(CLIENT_ID, "client-3");

        UserActivityEvent event = new UserActivityEvent(
                ActivityStatus.FAILED,
                "req-3",
                "product-service",
                "/api/v1/products",
                "DELETE",
                Instant.parse("2026-08-22T10:00:00Z"),
                Instant.parse("2026-08-22T10:00:01Z"),
                1000L,
                500,
                "boom"
        );

        userActivityLogger.log(exchange, event);

        assertThat(MDC.get("existingKey")).isEqualTo("existingValue");
        assertThat(MDC.get(CLIENT_ID)).isNull();
        assertThat(MDC.get("ipAddress")).isNull();
    }

    private ServerWebExchange createExchange(String forwardedFor, InetSocketAddress remoteAddress) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/product-service/api/v1/products")
                .header(PAGE_URL_HEADER, "/home")
                .header(USER_AGENT_HEADER, "JUnit-Agent")
                .remoteAddress(remoteAddress);

        if (forwardedFor != null) {
            requestBuilder.header("X-Forwarded-For", forwardedFor);
        }

        return MockServerWebExchange.from(requestBuilder);
    }
}
