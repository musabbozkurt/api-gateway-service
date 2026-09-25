package com.mb.apigateway.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.apigateway.config.UserActivityElasticsearchProperties;
import com.mb.apigateway.enums.ActivityStatus;
import com.mb.apigateway.logging.UserActivityEvent;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.mb.apigateway.constant.GatewayServiceConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

class UserActivityPublisherServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final long REQUEST_WAIT_SECONDS = 5;
    private static final long NO_REQUEST_WAIT_MILLIS = 300;
    private static final long MAX_PUBLISH_CALL_MILLIS = 1000;

    private MockWebServer server;
    private String currentIndexPath;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        currentIndexPath = "/user-activity-" + DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC).format(Instant.now());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void publish_ShouldCreateIndexAndWriteDocument_WhenIndexMissing() throws Exception {
        // Arrange
        enqueue(HttpStatus.NOT_FOUND);
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.CREATED);

        ServerWebExchange exchange = createExchange("1.1.1.1, 2.2.2.2");
        exchange.getAttributes().put(CLIENT_ID, "client-1");
        exchange.getAttributes().put(USER_ID, "user-1");
        exchange.getAttributes().put(USERNAME, "tester");
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "elastic", "secret");

        // Act
        publisher.publish(exchange, completedEvent());

        // Assertions
        RecordedRequest head = takeRequest(HttpMethod.HEAD);
        assertThat(head.getPath()).isEqualTo(currentIndexPath);
        assertThat(head.getHeader(HttpHeaders.AUTHORIZATION)).startsWith("Basic ");

        RecordedRequest put = takeRequest(HttpMethod.PUT);
        JsonNode mappings = readBody(put).path("mappings").path("properties");
        assertThat(mappings.path(TIMESTAMP_FIELD).path("type").asText()).isEqualTo("date");
        assertThat(mappings.path(DURATION_MS).path("type").asText()).isEqualTo("long");

        RecordedRequest post = takeRequest(HttpMethod.POST);
        assertThat(post.getPath()).isEqualTo(currentIndexPath + "/_doc");

        JsonNode document = readBody(post);
        assertThat(document.path(CLIENT_ID).asText()).isEqualTo("client-1");
        assertThat(document.path(USER_ID).asText()).isEqualTo("user-1");
        assertThat(document.path(USERNAME).asText()).isEqualTo("tester");
        assertThat(document.path(STATUS).asText()).isEqualTo(ActivityStatus.COMPLETED.name());
        assertThat(document.path(EVENT_TYPE).asText()).isEqualTo(USER_ACTIVITY_EVENT_TYPE);
        assertThat(document.path(IP_ADDRESS).asText()).isEqualTo("1.1.1.1");
        assertThat(document.path(DEVICE_INFO_HEADER).asText()).isEqualTo("JUnit-Agent");
        assertThat(document.path(PAGE_URL_HEADER).asText()).isEqualTo("/home");
        assertThat(document.path(DURATION_MS).asLong()).isEqualTo(1000L);
        assertThat(document.path(HTTP_STATUS).asInt()).isEqualTo(HttpStatus.OK.value());
        assertThat(document.hasNonNull(TIMESTAMP_FIELD)).isTrue();
        assertThat(document.has(ERROR_MESSAGE)).isFalse();
    }

    @Test
    void publish_ShouldUseRemoteAddressAndCheckIndexOnlyOnce_WhenIndexExists() throws Exception {
        // Arrange
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.CREATED);
        enqueue(HttpStatus.CREATED);
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");

        // Act
        publisher.publish(createExchange(null), completedEvent());
        takeRequest(HttpMethod.HEAD);
        takeRequest(HttpMethod.PUT);
        RecordedRequest first = takeRequest(HttpMethod.POST);

        publisher.publish(createExchange(null), completedEvent());
        RecordedRequest second = takeRequest(HttpMethod.POST);

        // Assertions
        assertThat(readBody(first).path(IP_ADDRESS).asText()).isEqualTo("10.0.0.7");
        assertThat(second.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
        assertThat(server.getRequestCount()).isEqualTo(4);
    }

    @Test
    void publish_ShouldRetryIndexInitialization_WhenPreviousAttemptFailed() throws Exception {
        // Arrange
        enqueue(HttpStatus.INTERNAL_SERVER_ERROR);
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.CREATED);
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");

        // Act
        publisher.publish(createExchange(null), completedEvent());

        // Assertions - failed initialization must not write the document
        takeRequest(HttpMethod.HEAD);
        assertNoMoreRequests();

        // Act
        publisher.publish(createExchange(null), completedEvent());

        // Assertions - next event retries initialization and writes the document
        takeRequest(HttpMethod.HEAD);
        takeRequest(HttpMethod.PUT);
        takeRequest(HttpMethod.POST);
    }

    @Test
    void publish_ShouldTolerateConcurrentIndexCreation() throws Exception {
        // Arrange
        enqueue(HttpStatus.NOT_FOUND);
        enqueueBadRequest("""
                {
                    "error": {
                        "type": "%s"
                    }
                }
                """.formatted(INDEX_ALREADY_EXISTS));
        enqueue(HttpStatus.CREATED);
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");

        // Act
        publisher.publish(createExchange(null), completedEvent());

        // Assertions
        takeRequest(HttpMethod.HEAD);
        takeRequest(HttpMethod.PUT);
        takeRequest(HttpMethod.POST);
    }

    @Test
    void publish_ShouldNotWriteDocument_WhenIndexCreationIsRejected() throws Exception {
        // Arrange
        enqueue(HttpStatus.NOT_FOUND);
        enqueueBadRequest("""
                {
                    "error": {
                        "type": "mapper_parsing_exception"
                    }
                }
                """);
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");

        // Act
        publisher.publish(createExchange(null), completedEvent());

        // Assertions
        takeRequest(HttpMethod.HEAD);
        takeRequest(HttpMethod.PUT);
        assertNoMoreRequests();
    }

    @Test
    void publish_ShouldNotSendAnything_WhenDisabled() throws Exception {
        // Arrange
        UserActivityPublisherServiceImpl publisher = createPublisher(false, "", "");

        // Act
        publisher.publish(createExchange(null), completedEvent());

        // Assertions
        assertNoMoreRequests();
    }

    @Test
    void publish_ShouldReturnImmediatelyAndNotThrow_WhenElasticsearchIsDown() throws IOException {
        // Arrange
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");
        server.shutdown();
        long start = System.nanoTime();

        // Act
        ThrowingCallable publishCall = () -> publisher.publish(createExchange(null), completedEvent());

        // Assertions
        assertThatCode(publishCall).doesNotThrowAnyException();
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(MAX_PUBLISH_CALL_MILLIS);
    }

    @Test
    void publish_ShouldReturnImmediately_WhenElasticsearchHangs() throws Exception {
        // Arrange
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.OK);
        enqueue(HttpStatus.CREATED);
        enqueue(HttpStatus.CREATED);
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");
        long start = System.nanoTime();

        // Act
        ThrowingCallable publishCall = () -> publisher.publish(createExchange(null), completedEvent());

        // Assertions - the caller is never blocked by a hanging Elasticsearch
        assertThatCode(publishCall).doesNotThrowAnyException();
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(MAX_PUBLISH_CALL_MILLIS);
        takeRequest(HttpMethod.HEAD);

        // Act - keep publishing until the timed-out initialization is evicted and a new one starts
        AtomicReference<RecordedRequest> retriedRequest = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(REQUEST_WAIT_SECONDS))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> {
                    publisher.publish(createExchange(null), completedEvent());
                    retriedRequest.set(server.takeRequest(100, TimeUnit.MILLISECONDS));
                    return retriedRequest.get() != null;
                });

        // Assertions - the publisher recovers and writes the document
        assertThat(retriedRequest.get().getMethod()).isEqualTo(HttpMethod.HEAD.name());
        takeRequest(HttpMethod.PUT);
        takeRequest(HttpMethod.POST);
    }

    @Test
    void publish_ShouldNotThrow_WhenInputIsIncomplete() {
        // Arrange
        UserActivityPublisherServiceImpl publisher = createPublisher(true, "", "");
        UserActivityEvent eventWithoutStatus = new UserActivityEvent(null, "req-4", null, null, null, null, null, null, null, null);

        // Act
        ThrowingCallable publishCalls = () -> {
            publisher.publish(createExchange(null), eventWithoutStatus);
            publisher.publish(createExchange(null), null);
            publisher.publish(null, completedEvent());
        };

        // Assertions
        assertThatCode(publishCalls).doesNotThrowAnyException();
    }

    private void enqueue(HttpStatus status) {
        server.enqueue(new MockResponse().setResponseCode(status.value()));
    }

    private void enqueueBadRequest(String body) {
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()).setBody(body));
    }

    private RecordedRequest takeRequest(HttpMethod expectedMethod) throws InterruptedException {
        RecordedRequest request = server.takeRequest(REQUEST_WAIT_SECONDS, TimeUnit.SECONDS);
        assertThat(request).as("expected %s request to Elasticsearch", expectedMethod).isNotNull();
        assertThat(request.getMethod()).isEqualTo(expectedMethod.name());
        return request;
    }

    private void assertNoMoreRequests() throws InterruptedException {
        assertThat(server.takeRequest(NO_REQUEST_WAIT_MILLIS, TimeUnit.MILLISECONDS)).isNull();
    }

    private JsonNode readBody(RecordedRequest request) throws IOException {
        return OBJECT_MAPPER.readTree(request.getBody().readUtf8());
    }

    private UserActivityEvent completedEvent() {
        return new UserActivityEvent(
                ActivityStatus.COMPLETED,
                "req-1",
                "product-service",
                "/api/v1/products",
                HttpMethod.GET.name(),
                Instant.parse("2026-08-22T10:00:00Z"),
                Instant.parse("2026-08-22T10:00:01Z"),
                1000L,
                HttpStatus.OK.value(),
                null
        );
    }

    private ServerWebExchange createExchange(String forwardedFor) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/product-service/api/v1/products")
                .header(PAGE_URL_HEADER, "/home")
                .header(USER_AGENT_HEADER, "JUnit-Agent")
                .remoteAddress(new InetSocketAddress("10.0.0.7", 8080));

        if (forwardedFor != null) {
            requestBuilder.header(FORWARDED_FOR_HEADER, forwardedFor);
        }

        return MockServerWebExchange.from(requestBuilder);
    }

    private UserActivityPublisherServiceImpl createPublisher(boolean enabled, String username, String password) {
        UserActivityElasticsearchProperties properties = new UserActivityElasticsearchProperties();
        properties.setEnabled(enabled);
        properties.setBaseUrl(server.url("/").toString());
        properties.setUsername(username);
        properties.setPassword(password);
        properties.setIndexPrefix("user-activity");
        return new UserActivityPublisherServiceImpl(WebClient.builder(), properties);
    }
}
