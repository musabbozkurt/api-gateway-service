package com.mb.apigateway.filter;

import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for the Redis-based request rate limiter.
 * <p>
 * Uses {@link MockWebServer} instead of MockServerContainer to avoid
 * client/server version incompatibility (mockserver-client-java 6.x
 * is not compatible with the 5.x Docker image).
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterIntegrationTest {

    @Container
    private static final RedisContainer redis = new RedisContainer("redis:8.6.1")
            .withExposedPorts(6379);

    /**
     * In-process HTTP server that returns 200 for every request, simulating the downstream service.
     */
    private static MockWebServer mockBackendServer;

    @MockitoBean(name = "opaqueTokenIntrospector")
    private ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector;

    @MockitoBean(name = "stockExchangeTokenIntrospector")
    private ReactiveOpaqueTokenIntrospector stockExchangeTokenIntrospector;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockBackendServer = new MockWebServer();
        mockBackendServer.setDispatcher(new Dispatcher() {
            @NonNull
            @Override
            public MockResponse dispatch(@NonNull RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                    "message": "Success"
                                }
                                """);
            }
        });
        mockBackendServer.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        if (mockBackendServer != null) {
            mockBackendServer.shutdown();
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Redis properties
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        String mockBackendUrl = "http://localhost:" + mockBackendServer.getPort();
        log.info("MockWebServer URL: {}", mockBackendUrl);

        // Fully override gateway config (routes + default-filters) to isolate the test
        // from application.yml, which references unresolved service URLs.
        registry.add("spring.cloud.gateway.server.webflux.default-filters[0]", () -> "StripPrefix=1");
        registry.add("spring.cloud.gateway.server.webflux.default-filters[1].name", () -> "RequestRateLimiter");
        registry.add("spring.cloud.gateway.server.webflux.default-filters[1].args.redis-rate-limiter.replenishRate", () -> "40");
        registry.add("spring.cloud.gateway.server.webflux.default-filters[1].args.redis-rate-limiter.burstCapacity", () -> "80");
        registry.add("spring.cloud.gateway.server.webflux.default-filters[1].args.redis-rate-limiter.requestedTokens", () -> "1");

        // Configure the route
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "students-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> mockBackendUrl);
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/students/**");
    }

    @BeforeEach
    void setUp() {
        // Ensure Redis is reachable and warm up the Lettuce connection pool.
        // The rate limiter fails-open (allows all) when Redis is unreachable.
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(reactiveRedisTemplate.getConnectionFactory()
                        .getReactiveConnection()
                        .ping()
                        .block()).isEqualTo("PONG"));

        // Flush Redis so each test starts with a full token bucket (burstCapacity = 80)
        reactiveRedisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .serverCommands()
                .flushAll()
                .block();

        OAuth2AuthenticatedPrincipal principal = new OAuth2AuthenticatedPrincipal() {
            @Override
            public Map<String, Object> getAttributes() {
                return Map.of(
                        "active", true,
                        "sub", "rate-limiter-test-user",
                        "client_id", "test_client_id",
                        "userId", "test_user_id",
                        "user_name", "test_username"
                );
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @NonNull
            @Override
            public String getName() {
                return "rate-limiter-test-user";
            }
        };

        when(opaqueTokenIntrospector.introspect(anyString())).thenReturn(Mono.just(principal));
        when(stockExchangeTokenIntrospector.introspect(anyString())).thenReturn(Mono.just(principal));
    }

    @Test
    void rateLimiter_ShouldAllowInitialRequestsAndThenLimit_WhenSendingMultipleRequests() {
        // Arrange
        WebTestClient client = buildClient();
        warmUpRateLimiter(client);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Act
        IntStream.range(0, 200).forEach(_ -> {
            try {
                HttpStatusCode status = sendAuthenticatedRequest(client, "/students/test");
                if (status.equals(HttpStatus.OK)) {
                    successCount.incrementAndGet();
                } else if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                    rejectedCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                    log.error("Unexpected status: {} (value: {})", status, status.value());
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("Exception occurred during test: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
        });

        log.info("Success count: {}, Rejected count: {}, Error count: {}", successCount.get(), rejectedCount.get(), errorCount.get());

        // Assertions
        assertThat(errorCount.get()).as("Unexpected errors encountered").isZero();
        assertThat(successCount.get()).as("Expected some successful requests").isGreaterThan(0);
        assertThat(rejectedCount.get()).as("Expected rate limiting to occur").isGreaterThan(0);
    }

    @Test
    void rateLimiter_ShouldReturn200_WhenSingleRequestIsSent() {
        // Arrange
        WebTestClient client = buildClient();

        // Act
        HttpStatusCode status = sendAuthenticatedRequest(client, "/students/test");

        // Assertions
        assertThat(status).as("Single request should be allowed").isEqualTo(HttpStatus.OK);
    }

    @Test
    void rateLimiter_ShouldReturn429_WhenBurstCapacityIsExhausted() {
        // Arrange
        WebTestClient client = buildClient();
        warmUpRateLimiter(client);

        // Act — exhaust burst capacity + replenished tokens (burstCapacity=80, replenishRate=40/sec)
        IntStream.range(0, 150).forEach(_ -> sendAuthenticatedRequest(client, "/students/test"));

        AtomicInteger rejectedCount = new AtomicInteger(0);
        IntStream.range(0, 10).forEach(_ -> {
            HttpStatusCode status = sendAuthenticatedRequest(client, "/students/test");
            if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                rejectedCount.incrementAndGet();
            }
        });

        // Assertions
        assertThat(rejectedCount.get()).as("Expected requests to be rate-limited after burst exhaustion").isGreaterThan(0);
    }

    @Test
    void rateLimiter_ShouldRateLimitConcurrentRequests_WhenTrafficIsParallel() throws InterruptedException {
        // Arrange
        WebTestClient client = buildClient();
        int totalRequests = 150;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        // Act — send requests concurrently to simulate real traffic
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            CountDownLatch latch = new CountDownLatch(totalRequests);

            IntStream.range(0, totalRequests).forEach(_ -> executor.submit(() -> {
                try {
                    HttpStatusCode status = sendAuthenticatedRequest(client, "/students/test");
                    if (status.equals(HttpStatus.OK)) {
                        successCount.incrementAndGet();
                    } else if (status.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        rejectedCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            }));

            latch.await();
        }

        log.info("Concurrent test — Success: {}, Rejected: {}", successCount.get(), rejectedCount.get());

        // Assertions
        assertThat(successCount.get() + rejectedCount.get()).as("All requests should return either 200 or 429").isEqualTo(totalRequests);
        assertThat(successCount.get()).as("Some concurrent requests should succeed").isGreaterThan(0);
        assertThat(rejectedCount.get()).as("Some concurrent requests should be rate-limited").isGreaterThan(0);
    }

    @Test
    void rateLimiter_ShouldReplenishTokens_WhenWaitingBetweenBursts() {
        // Arrange
        WebTestClient client = buildClient();

        // Act — exhaust most of the burst capacity
        IntStream.range(0, 75).forEach(_ -> sendAuthenticatedRequest(client, "/students/test"));

        // Wait for replenishment (replenishRate = 40 tokens/sec → ~1s should add ~40 tokens)
        AtomicInteger successCount = new AtomicInteger(0);
        await().atMost(3, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    successCount.set(0);
                    IntStream.range(0, 10).forEach(_ -> {
                        HttpStatusCode status = sendAuthenticatedRequest(client, "/students/test");
                        if (status.equals(HttpStatus.OK)) {
                            successCount.incrementAndGet();
                        }
                    });
                    assertThat(successCount.get()).as("Tokens should have replenished after waiting").isGreaterThan(0);
                });
    }

    @Test
    void rateLimiter_ShouldRouteSuccessfully_WhenDifferentSubPathsAreRequested() {
        // Arrange
        WebTestClient client = buildClient();

        // Act — different sub-paths under /students/** share the same rate-limit bucket (same IP key)
        HttpStatusCode status1 = sendAuthenticatedRequest(client, "/students/test");
        HttpStatusCode status2 = sendAuthenticatedRequest(client, "/students/another-path");
        HttpStatusCode status3 = sendAuthenticatedRequest(client, "/students/123/details");

        // Assertions
        assertThat(status1).isEqualTo(HttpStatus.OK);
        assertThat(status2).isEqualTo(HttpStatus.OK);
        assertThat(status3).isEqualTo(HttpStatus.OK);
    }

    // --- Helper methods ---

    /**
     * Sends a single request and waits until the rate limiter is actively connected to Redis.
     * Without this, the rate limiter may fail-open (allow all) on first requests if the
     * Lettuce connection pool hasn't been established yet.
     */
    private void warmUpRateLimiter(WebTestClient client) {
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    sendAuthenticatedRequest(client, "/students/warmup");
                    // After warmup, verify rate limiter is working by checking Redis has rate-limit keys
                    Long keyCount = reactiveRedisTemplate.keys("request_rate_limiter.*")
                            .count()
                            .block(Duration.ofSeconds(2));
                    return keyCount != null && keyCount > 0;
                });
    }

    private WebTestClient buildClient() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    private HttpStatusCode sendAuthenticatedRequest(WebTestClient client, String uri) {
        return client.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature")
                .exchange()
                .returnResult(String.class)
                .getStatus();
    }
}
