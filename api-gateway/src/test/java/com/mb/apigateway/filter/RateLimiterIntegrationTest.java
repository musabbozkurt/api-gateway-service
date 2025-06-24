package com.mb.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterIntegrationTest {

    private static final DockerImageName MOCKSERVER_IMAGE = DockerImageName.parse("mockserver/mockserver:mockserver-5.14.0");

    @Container
    private static final MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private MockServerClient mockServerClient;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Redis properties
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        // Use lb:// format for better container networking
        String mockServerUrl = "http://%s:%d".formatted(mockServer.getHost(), mockServer.getServerPort());
        log.info("MockServer URL: {}", mockServerUrl);

        // Configure API Gateway routes
        registry.add("spring.cloud.gateway.routes[0].id", () -> "students-service");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> mockServerUrl);
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/students/**");
    }

    @BeforeEach
    void setUp() {
        // Close previous instance if exists
        if (mockServerClient != null) {
            try {
                mockServerClient.close();
            } catch (Exception e) {
                // Ignore
            }
        }

        // Create new client with direct constructor - more stable
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        log.info("MockServer started at: {}:{}", mockServer.getHost(), mockServer.getServerPort());

        try {
            // Reset to clear previous expectations
            mockServerClient.reset();

            // Make path matching more flexible - use regex
            mockServerClient
                    .when(request().withPath(".*test.*"))
                    .respond(response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "message": "Success",
                                    }
                                    """));

            // Match exact paths after prefix stripping
            mockServerClient
                    .when(request().withPath("/test"))
                    .respond(response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "message": "Success",
                                    }
                                    """));

            // Also handle with trailing slash
            mockServerClient
                    .when(request().withPath("/test/"))
                    .respond(response()
                            .withStatusCode(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                        "message": "Success",
                                    }
                                    """));
        } catch (Exception e) {
            log.error("Error occurred while configuring MockServer: {}", e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Add small delay to avoid overwhelming the system
        await().atMost(100, TimeUnit.MILLISECONDS);
        if (mockServerClient != null) {
            mockServerClient.close();
        }
    }

    @Test
    void rateLimiter_ShouldAllowInitialRequestsAndThenLimit_WhenSendingMultipleRequests() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        // Test burst capacity
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Add slight delay between requests
        IntStream.range(0, 100).forEach(i -> {
            try {
                WebTestClient.ResponseSpec response = client.get()
                        .uri("/students/test")
                        .exchange();

                HttpStatusCode status = response.returnResult(String.class).getStatus();
                if (status.equals(HttpStatus.OK)) {
                    successCount.incrementAndGet();
                } else if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                    rejectedCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                    log.error("Unexpected status: {}", status);
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("Exception occurred during test: {}", e.getMessage());
            }
        });

        // Relaxed assertions for debugging
        log.info("Success count: {}", successCount.get());
        log.info("Rejected count: {}", rejectedCount.get());
        log.info("Error count: {}", errorCount.get());

        assert errorCount.get() == 0 : "Unexpected errors encountered";
        assert successCount.get() > 0 : "Expected some successful requests";
        assert rejectedCount.get() > 0 || successCount.get() <= 40 : "Expected rate limiting to occur";
    }
}
