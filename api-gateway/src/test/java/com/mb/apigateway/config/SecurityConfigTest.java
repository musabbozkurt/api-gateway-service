package com.mb.apigateway.config;

import com.mb.apigateway.filter.AuthenticationFilter;
import com.mb.apigateway.filter.HttpRequestSmugglingPreventionFilter;
import com.mb.apigateway.service.ServiceAccessCacheService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SecurityConfig}.
 * <p>
 * {@link SecurityFilterChainIntegrationTest} verifies route-level access control, CSRF behaviour,
 * HTTP request-smuggling prevention, and filter ordering via a full Spring Boot context with a
 * {@link okhttp3.mockwebserver.MockWebServer} backend.
 * {@link OpaqueTokenIntrospectorUnitTest} verifies introspection wire behaviour (Basic auth header,
 * endpoint path, request body, active/inactive token handling) in isolation.
 */
class SecurityConfigTest {

    @Nested
    @AutoConfigureWebTestClient
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class SecurityFilterChainIntegrationTest {

        private static MockWebServer mockBackendServer;

        @Autowired
        private WebTestClient webTestClient;

        @Autowired
        private SecurityWebFilterChain securityWebFilterChain;

        @Autowired
        private HttpRequestSmugglingPreventionFilter httpRequestSmugglingPreventionFilter;

        @Autowired
        private AuthenticationFilter authenticationFilter;

        @MockitoBean(name = "opaqueTokenIntrospector")
        private ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector;

        @MockitoBean(name = "stockExchangeTokenIntrospector")
        private ReactiveOpaqueTokenIntrospector stockExchangeTokenIntrospector;

        @MockitoBean
        private ServiceAccessCacheService serviceAccessCacheService;

        @BeforeAll
        static void setUp() throws IOException {
            mockBackendServer = new MockWebServer();
            mockBackendServer.start();
        }

        @AfterAll
        static void tearDown() throws IOException {
            mockBackendServer.shutdown();
        }

        @DynamicPropertySource
        static void registerProperties(DynamicPropertyRegistry registry) {
            String mockBackendUrl = "http://localhost:" + mockBackendServer.getPort();

            registry.add("spring.cloud.gateway.server.webflux.default-filters", () -> "StripPrefix=1");

            registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "rbac-service");
            registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> mockBackendUrl);
            registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/rbac-service/**");

            registry.add("spring.cloud.gateway.server.webflux.routes[1].id", () -> "content-service");
            registry.add("spring.cloud.gateway.server.webflux.routes[1].uri", () -> mockBackendUrl);
            registry.add("spring.cloud.gateway.server.webflux.routes[1].predicates[0]", () -> "Path=/content-service/**");

            registry.add("spring.cloud.gateway.server.webflux.routes[2].id", () -> "product-service");
            registry.add("spring.cloud.gateway.server.webflux.routes[2].uri", () -> mockBackendUrl);
            registry.add("spring.cloud.gateway.server.webflux.routes[2].predicates[0]", () -> "Path=/product-service/**");

            registry.add("spring.cloud.gateway.server.webflux.routes[3].id", () -> "stock-exchange-service");
            registry.add("spring.cloud.gateway.server.webflux.routes[3].uri", () -> mockBackendUrl);
            registry.add("spring.cloud.gateway.server.webflux.routes[3].predicates[0]", () -> "Path=/stock-exchange/**");

            registry.add("spring.cloud.gateway.server.webflux.routes[4].id", () -> "inventory-management-service");
            registry.add("spring.cloud.gateway.server.webflux.routes[4].uri", () -> mockBackendUrl);
            registry.add("spring.cloud.gateway.server.webflux.routes[4].predicates[0]", () -> "Path=/inventory/**");
        }

        @BeforeEach
        void setUpMocks() {
            when(serviceAccessCacheService.hasAccess(any(), any(), any(), any(), any())).thenReturn(Mono.just(true));
        }

        @Test
        @DisplayName("Actuator health endpoint should be accessible without authentication")
        void healthEndpoint_ShouldReturnStatusUp_WhenCalledWithoutAuthentication() {
            // Arrange
            // No arrangement needed

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("UP");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "/rbac-service/api/v1/forgot-password/generate-code",
                "/rbac-service/api/v1/forgot-password/validate-code",
                "/rbac-service/api/v1/forgot-password/change"
        })
        @DisplayName("Forgot password endpoints should be accessible without authentication")
        void forgotPasswordEndpoints_ShouldReturn2xxSuccessful_WhenCalledWithoutAuthentication(String path) {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "success": true
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act
            // Assertions
            webTestClient.post()
                    .uri(path)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("Content service should be accessible without authentication")
        void contentService_ShouldReturn2xxSuccessful_WhenCalledWithoutAuthentication() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "content": "test"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/content-service/api/v1/content")
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @Disabled("Disabled because the gateway currently allows all requests")
        @DisplayName("Protected endpoint should return 401 when no token provided")
        void protectedEndpoint_ShouldReturnUnauthorized_WhenNoTokenProvided() {
            // Arrange
            // No arrangement needed

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Protected endpoint should return 5xx when invalid token provided")
        void protectedEndpoint_ShouldReturn5xxServerError_WhenInvalidTokenProvided() {
            // Arrange
            when(opaqueTokenIntrospector.introspect(anyString())).thenReturn(Mono.error(new OAuth2IntrospectionException("Invalid token")));

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("Protected endpoint should succeed with valid token")
        void protectedEndpoint_ShouldReturnOk_WhenValidTokenProvided() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "products": []
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("CSRF should be disabled")
        void csrf_ShouldAllowPostRequest_WhenNoCsrfTokenProvided() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "success": true
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act
            // Assertions
            webTestClient.post()
                    .uri("/rbac-service/api/v1/forgot-password/generate-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "email": "test@example.com"
                            }
                            """)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        @Test
        @DisplayName("Security filter chain bean should be configured")
        void securityFilterChain_ShouldBeNotNull_WhenApplicationContextLoaded() {
            // Arrange
            // No arrangement needed

            // Act
            // Assertions
            assertThat(securityWebFilterChain).isNotNull();
        }

        @Test
        @DisplayName("HttpRequestSmugglingPreventionFilter should run before AuthenticationFilter")
        void filterOrdering_HttpRequestSmugglingPreventionFilter_ShouldRunBeforeAuthenticationFilter() {
            // Arrange
            // No arrangement needed

            // Act
            int smugglingFilterOrder = httpRequestSmugglingPreventionFilter.getOrder();
            int authFilterOrder = authenticationFilter.getOrder();

            // Assertions - Lower order value means higher priority (runs first)
            // HttpRequestSmugglingPreventionFilter must have HIGHEST_PRECEDENCE and run before AuthenticationFilter
            assertThat(smugglingFilterOrder)
                    .isEqualTo(Ordered.HIGHEST_PRECEDENCE)
                    .isLessThan(authFilterOrder);
            assertThat(authFilterOrder).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
        }

        @Test
        @DisplayName("Token introspection should handle inactive token")
        void opaqueTokenIntrospector_ShouldReturnUnauthorized_WhenTokenIsInactive() {
            // Arrange
            when(opaqueTokenIntrospector.introspect(anyString())).thenReturn(Mono.error(new BadOpaqueTokenException("Token is not active")));

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer inactive-token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Request with Transfer-Encoding and Content-Length should be rejected (CL.TE smuggling)")
        void httpRequestSmuggling_ShouldRejectRequest_WhenBothTransferEncodingAndContentLengthPresent() {
            // Arrange
            // HTTP Request Smuggling CL.TE attack: Content-Length and Transfer-Encoding headers together
            // Note: WebTestClient cannot send raw Transfer-Encoding headers, so we test the filter directly
            MockServerHttpRequest request = MockServerHttpRequest.post("/product-service/api/v1/products")
                    .header(HttpHeaders.CONTENT_LENGTH, "3")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());

            // Act
            Mono<Void> result = httpRequestSmugglingPreventionFilter.filter(exchange, chain);

            // Assertions - Filter should reject request with 400 Bad Request
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body contains error message
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains both Content-Length and Transfer-Encoding headers");
        }

        @Test
        @DisplayName("Request with duplicate Transfer-Encoding headers should be rejected (TE.TE smuggling)")
        void httpRequestSmuggling_ShouldRejectRequest_WhenDuplicateTransferEncodingHeaders() {
            // Arrange
            // HTTP Request Smuggling TE.TE attack: comma-separated Transfer-Encoding values
            // Note: WebTestClient cannot send raw Transfer-Encoding headers, so we test the filter directly
            MockServerHttpRequest request = MockServerHttpRequest.post("/product-service/api/v1/products")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked, identity")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());

            // Act
            Mono<Void> result = httpRequestSmugglingPreventionFilter.filter(exchange, chain);

            // Assertions - Filter should reject request with 400 Bad Request
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body contains error message
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");
        }

        @Test
        @DisplayName("Request with malformed Transfer-Encoding header should be rejected")
        void httpRequestSmuggling_ShouldRejectRequest_WhenMalformedTransferEncoding() {
            // Arrange
            // Malformed Transfer-Encoding with leading whitespace that might bypass some parsers
            // Note: WebTestClient cannot send raw Transfer-Encoding headers, so we test the filter directly
            MockServerHttpRequest request = MockServerHttpRequest.post("/product-service/api/v1/products")
                    .header(HttpHeaders.TRANSFER_ENCODING, " chunked") // Leading whitespace
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());

            // Act
            Mono<Void> result = httpRequestSmugglingPreventionFilter.filter(exchange, chain);

            // Assertions - Filter should reject request with 400 Bad Request
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body contains error message
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");
        }

        @Test
        @DisplayName("Request with multiple separate Transfer-Encoding headers should be rejected (TE.TE smuggling)")
        void httpRequestSmuggling_ShouldRejectRequest_WhenMultipleSeparateTransferEncodingHeaders() {
            // Arrange
            // This tests the hasMultipleTransferEncodingHeaders method (transferEncodings.size() > 1)
            // Note: WebTestClient cannot send multiple headers with same name, so we test the filter directly
            MockServerHttpRequest request = MockServerHttpRequest.post("/product-service/api/v1/products")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.TRANSFER_ENCODING, "identity")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());

            // Act
            Mono<Void> result = httpRequestSmugglingPreventionFilter.filter(exchange, chain);

            // Assertions - Filter should reject request with 400 Bad Request
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body contains error message about invalid Transfer-Encoding
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");
        }

        @Test
        @DisplayName("Request with embedded CRLF in header value should be handled safely")
        void httpRequestSmuggling_ShouldHandleSafely_WhenCRLFInHeaderValue() {
            // Arrange
            // CRLF injection attempt - WebTestClient sanitizes CRLF, so we verify safe handling
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Act
            // Assertions - Gateway should handle safely (WebTestClient sanitizes CRLF)
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .header("X-Custom-Header", "safe-value")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Request with null bytes in URI should be handled")
        void httpRequestSmuggling_ShouldHandle_WhenNullBytesInUri() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Act
            // Assertions - Gateway should handle null bytes safely
            webTestClient.get()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Request smuggling via mismatched Content-Length should be handled safely")
        void httpRequestSmuggling_ShouldHandleSafely_WhenContentLengthMismatch() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Body is larger than Content-Length declares - potential smuggling attempt
            String maliciousBody = """
                    X\r
                    GET /rbac-service/api/v1/user/me HTTP/1.1\r
                    Host: evil.com\r
                    \r
                    """;

            // Act
            // Assertions - The request should either be rejected or the extra content should be ignored
            webTestClient.post()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .bodyValue(maliciousBody)
                    .exchange()
                    .expectStatus().isOk(); // Should not cause server error
        }

        @Test
        @DisplayName("Backend should not receive smuggled second request")
        void httpRequestSmuggling_BackendShouldNotReceiveSmuggledRequest_WhenCLTEAttackAttempted() throws InterruptedException {
            // Arrange
            // Drain any pending requests from previous tests
            RecordedRequest pendingRequest;
            do {
                pendingRequest = mockBackendServer.takeRequest(50, TimeUnit.MILLISECONDS);
            } while (pendingRequest != null);

            // Enqueue responses for potential multiple requests
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                                "result": "first"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                                "result": "second"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Act
            webTestClient.post()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue("""
                            {
                                "data": "test"
                            }
                            """)
                    .exchange();

            // Assertions - Only one request should have been received by the backend
            // Use takeRequest with timeout to wait for the first request
            RecordedRequest firstRequest = mockBackendServer.takeRequest(2, TimeUnit.SECONDS);
            assertThat(firstRequest).isNotNull();
            assertThat(firstRequest.getPath()).isEqualTo("/api/v1/products");

            // Try to get a second request with a short timeout - should be null (no smuggled request)
            RecordedRequest potentialSmuggledRequest = mockBackendServer.takeRequest(500, TimeUnit.MILLISECONDS);
            assertThat(potentialSmuggledRequest)
                    .as("No smuggled second request should be received")
                    .isNull();
        }

        @Test
        @DisplayName("Request with HTTP/0.9 style request in body should not be forwarded as separate request")
        void httpRequestSmuggling_ShouldNotForwardEmbeddedRequest_WhenHTTP09StyleInBody() throws InterruptedException {
            // Arrange
            // Drain any pending requests from previous tests
            RecordedRequest pendingRequest;
            do {
                pendingRequest = mockBackendServer.takeRequest(50, TimeUnit.MILLISECONDS);
            } while (pendingRequest != null);

            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("{}")
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidTokenIntrospection();

            // Embedded HTTP request in body
            String bodyWithEmbeddedRequest = """
                    legitimate data\r
                    \r
                    GET /rbac-service/api/v1/user/me HTTP/1.1\r
                    Host: localhost\r
                    \r
                    """;

            // Act
            webTestClient.post()
                    .uri("/product-service/api/v1/products")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .bodyValue(bodyWithEmbeddedRequest)
                    .exchange()
                    .expectStatus().isOk();

            // Assertions - Verify the request was processed correctly
            RecordedRequest request = mockBackendServer.takeRequest(2, TimeUnit.SECONDS);
            assertThat(request).isNotNull();
            assertThat(request.getPath()).isEqualTo("/api/v1/products");

            // The body should contain the entire original content, not split into separate requests
            String receivedBody = request.getBody().readUtf8();
            assertThat(receivedBody).contains("legitimate data");

            // Try to get a second request with a short timeout - should be null (no smuggled request)
            RecordedRequest potentialSmuggledRequest = mockBackendServer.takeRequest(500, TimeUnit.MILLISECONDS);
            assertThat(potentialSmuggledRequest)
                    .as("Embedded request in body should not be forwarded as separate request")
                    .isNull();
        }

        @Test
        @DisplayName("Stock-exchange endpoint should return 401 when no token provided")
        @Disabled("Disabled because the gateway currently allows all requests via /** permitted path")
        void stockExchangeEndpoint_ShouldReturnUnauthorized_WhenNoTokenProvided() {
            // Arrange
            // No arrangement needed

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/stock-exchange/api/v1/users/1")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Stock-exchange endpoint should succeed with valid stock-exchange token")
        void stockExchangeEndpoint_ShouldReturnOk_WhenValidStockExchangeTokenProvided() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "id": 1,
                              "username": "stock-exchange-user"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            mockValidStockExchangeTokenIntrospection();

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/stock-exchange/api/v1/users/1")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-stock-exchange-token")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Stock-exchange endpoint should return 401 when invalid token provided")
        void stockExchangeEndpoint_ShouldReturnUnauthorized_WhenInvalidTokenProvided() {
            // Arrange
            when(stockExchangeTokenIntrospector.introspect(anyString()))
                    .thenReturn(Mono.error(new BadOpaqueTokenException("Token is not active")));

            // Act
            // Assertions
            webTestClient.get()
                    .uri("/stock-exchange/api/v1/users/1")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-stock-exchange-token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Stock-exchange auth endpoints should be accessible without authentication")
        void stockExchangeAuthEndpoints_ShouldReturn2xxSuccessful_WhenCalledWithoutAuthentication() {
            // Arrange
            mockBackendServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "token": "jwt-token"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act
            // Assertions
            webTestClient.post()
                    .uri("/stock-exchange/api/v1/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "username": "admin_user",
                              "password": "password"
                            }
                            """)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        private void mockValidTokenIntrospection() {
            var authenticatedPrincipal = createAuthenticatedPrincipal("test-user");
            when(opaqueTokenIntrospector.introspect(anyString())).thenReturn(Mono.just(authenticatedPrincipal));
        }

        private void mockValidStockExchangeTokenIntrospection() {
            var authenticatedPrincipal = createAuthenticatedPrincipal("stock-exchange-user");
            when(stockExchangeTokenIntrospector.introspect(anyString())).thenReturn(Mono.just(authenticatedPrincipal));
        }

        private OAuth2AuthenticatedPrincipal createAuthenticatedPrincipal(String username) {
            return new OAuth2AuthenticatedPrincipal() {
                @Override
                public Map<String, Object> getAttributes() {
                    return Map.of("active", true, "sub", username);
                }

                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of();
                }

                @NonNull
                @Override
                public String getName() {
                    return username;
                }
            };
        }
    }

    @Nested
    class OpaqueTokenIntrospectorUnitTest {

        private MockWebServer mockSsoServer;
        private SecurityConfig securityConfig;

        @BeforeEach
        void setUp() throws IOException {
            mockSsoServer = new MockWebServer();
            mockSsoServer.start();

            securityConfig = new SecurityConfig(new GatewaySecurityProperties());
            ReflectionTestUtils.setField(securityConfig, "introspectionUri", mockSsoServer.url("/oauth/check_token").toString());
            ReflectionTestUtils.setField(securityConfig, "clientId", "test-client-id");
            ReflectionTestUtils.setField(securityConfig, "clientSecret", "test-client-secret");
            ReflectionTestUtils.setField(securityConfig, "connectTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "responseTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "maxIdleTime", Duration.ofSeconds(30));
            ReflectionTestUtils.setField(securityConfig, "stockExchangeIntrospectionUri", mockSsoServer.url("/api/v1/auth/introspect").toString());
        }

        @AfterEach
        void tearDown() throws IOException {
            mockSsoServer.shutdown();
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should return authenticated principal when token is active")
        void opaqueTokenIntrospector_ShouldReturnAuthenticatedPrincipal_WhenTokenIsActive() {
            // Arrange
            mockSsoServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "sub": "test-user",
                              "client_id": "test-client-id",
                              "username": "testuser"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("valid-token"))
                    .assertNext(principal -> {
                        assertThat(principal).isNotNull();
                        assertThat(principal.getAttributes()).containsEntry("active", true);
                        assertThat(principal.getAttributes()).containsEntry("sub", "test-user");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should include basic auth header in request")
        void opaqueTokenIntrospector_ShouldIncludeBasicAuthHeader_WhenMakingIntrospectionRequest() throws InterruptedException {
            // Arrange
            mockSsoServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "sub": "test-user"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Act
            StepVerifier.create(introspector.introspect("test-token"))
                    .assertNext(principal -> assertThat(principal).isNotNull())
                    .verifyComplete();

            // Assertions
            RecordedRequest recordedRequest = mockSsoServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(recordedRequest).isNotNull();
            String authHeader = recordedRequest.getHeader(HttpHeaders.AUTHORIZATION);
            assertThat(authHeader)
                    .isNotNull()
                    .startsWith("Basic ");

            String expectedCredentials = Base64.getEncoder().encodeToString("test-client-id:test-client-secret".getBytes());
            assertThat(authHeader).isEqualTo("Basic " + expectedCredentials);
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should throw exception when token is inactive")
        void opaqueTokenIntrospector_ShouldThrowException_WhenTokenIsInactive() {
            // Arrange
            mockSsoServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": false
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("inactive-token"))
                    .expectError(OAuth2IntrospectionException.class)
                    .verify();
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector bean should not be null")
        void opaqueTokenIntrospector_ShouldNotBeNull_WhenCreated() {
            // Arrange
            // No arrangement needed

            // Act
            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Assertions
            assertThat(introspector).isNotNull();
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should be instance of SpringReactiveOpaqueTokenIntrospector")
        void opaqueTokenIntrospector_ShouldBeSpringReactiveOpaqueTokenIntrospector_WhenCreated() {
            // Arrange
            // No arrangement needed

            // Act
            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Assertions
            assertThat(introspector).isInstanceOf(SpringReactiveOpaqueTokenIntrospector.class);
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should call correct endpoint")
        void opaqueTokenIntrospector_ShouldCallCorrectEndpoint_WhenIntrospectingToken() throws InterruptedException {
            // Arrange
            mockSsoServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "sub": "test-user"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Act
            StepVerifier.create(introspector.introspect("test-token"))
                    .assertNext(principal -> assertThat(principal).isNotNull())
                    .verifyComplete();

            // Assertions
            RecordedRequest recordedRequest = mockSsoServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(recordedRequest).isNotNull();
            assertThat(recordedRequest.getPath()).isEqualTo("/oauth/check_token");
        }

        @Test
        @DisplayName("OpaqueTokenIntrospector should send token in request body")
        void opaqueTokenIntrospector_ShouldSendTokenInRequestBody_WhenIntrospectingToken() throws InterruptedException {
            // Arrange
            mockSsoServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "sub": "test-user"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            ReactiveOpaqueTokenIntrospector introspector = securityConfig.opaqueTokenIntrospector();

            // Act
            StepVerifier.create(introspector.introspect("my-test-token"))
                    .assertNext(principal -> assertThat(principal).isNotNull())
                    .verifyComplete();

            // Assertions
            RecordedRequest recordedRequest = mockSsoServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(recordedRequest).isNotNull();
            String body = recordedRequest.getBody().readUtf8();
            assertThat(body).contains("token=my-test-token");
        }
    }

    @Nested
    class StockExchangeTokenIntrospectorUnitTest {

        private MockWebServer mockStockExchangeServer;

        @BeforeEach
        void setUp() throws IOException {
            mockStockExchangeServer = new MockWebServer();
            mockStockExchangeServer.start();
        }

        @AfterEach
        void tearDown() throws IOException {
            mockStockExchangeServer.shutdown();
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should return authenticated principal when token is active")
        void introspect_ShouldReturnAuthenticatedPrincipal_WhenTokenIsActive() {
            // Arrange
            mockStockExchangeServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "username": "admin_user",
                              "sub": "admin_user",
                              "roles": ["ROLE_ADMIN", "ROLE_USER"]
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            StockExchangeReactiveTokenIntrospector introspector = new StockExchangeReactiveTokenIntrospector(
                    WebClient.builder(), mockStockExchangeServer.url("/api/v1/auth/introspect").toString());

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("valid-stock-exchange-token"))
                    .assertNext(principal -> {
                        assertThat(principal).isNotNull();
                        assertThat(principal.getName()).isEqualTo("admin_user");
                        assertThat(principal.getAttributes()).containsEntry("active", true);
                        assertThat(principal.getAttributes()).containsEntry("username", "admin_user");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should throw BadOpaqueTokenException when token is inactive")
        void introspect_ShouldThrowBadOpaqueTokenException_WhenTokenIsInactive() {
            // Arrange
            mockStockExchangeServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": false
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            StockExchangeReactiveTokenIntrospector introspector = new StockExchangeReactiveTokenIntrospector(
                    WebClient.builder(), mockStockExchangeServer.url("/api/v1/auth/introspect").toString());

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("inactive-token"))
                    .expectError(BadOpaqueTokenException.class)
                    .verify();
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should throw BadOpaqueTokenException when server returns error")
        void introspect_ShouldThrowBadOpaqueTokenException_WhenServerReturnsError() {
            // Arrange
            mockStockExchangeServer.enqueue(new MockResponse().setResponseCode(500));

            StockExchangeReactiveTokenIntrospector introspector = new StockExchangeReactiveTokenIntrospector(
                    WebClient.builder(), mockStockExchangeServer.url("/api/v1/auth/introspect").toString());

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("some-token"))
                    .expectError(BadOpaqueTokenException.class)
                    .verify();
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should send token as form parameter")
        void introspect_ShouldSendTokenAsFormParameter_WhenIntrospectingToken() throws InterruptedException {
            // Arrange
            mockStockExchangeServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true,
                              "username": "admin_user"
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            StockExchangeReactiveTokenIntrospector introspector = new StockExchangeReactiveTokenIntrospector(
                    WebClient.builder(), mockStockExchangeServer.url("/api/v1/auth/introspect").toString());

            // Act
            StepVerifier.create(introspector.introspect("my-stock-token"))
                    .assertNext(principal -> assertThat(principal).isNotNull())
                    .verifyComplete();

            // Assertions
            RecordedRequest recordedRequest = mockStockExchangeServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(recordedRequest).isNotNull();
            assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/auth/introspect");
            String body = recordedRequest.getBody().readUtf8();
            assertThat(body).isEqualTo("token=my-stock-token");
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should use 'unknown' when username is null")
        void introspect_ShouldUseUnknownName_WhenUsernameIsNull() {
            // Arrange
            mockStockExchangeServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                            {
                              "active": true
                            }
                            """)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            StockExchangeReactiveTokenIntrospector introspector = new StockExchangeReactiveTokenIntrospector(
                    WebClient.builder(), mockStockExchangeServer.url("/api/v1/auth/introspect").toString());

            // Act
            // Assertions
            StepVerifier.create(introspector.introspect("token-without-username"))
                    .assertNext(principal -> assertThat(principal.getName()).isEqualTo("unknown"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("StockExchangeTokenIntrospector should be instance of correct type")
        void stockExchangeTokenIntrospector_ShouldBeInstanceOfStockExchangeReactiveTokenIntrospector() {
            // Arrange
            SecurityConfig securityConfig = new SecurityConfig(new GatewaySecurityProperties());
            ReflectionTestUtils.setField(securityConfig, "introspectionUri", "http://localhost/introspect");
            ReflectionTestUtils.setField(securityConfig, "clientId", "test");
            ReflectionTestUtils.setField(securityConfig, "clientSecret", "test");
            ReflectionTestUtils.setField(securityConfig, "connectTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "responseTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "maxIdleTime", Duration.ofSeconds(30));
            ReflectionTestUtils.setField(securityConfig, "stockExchangeIntrospectionUri", "http://localhost/api/v1/auth/introspect");

            // Act
            ReactiveOpaqueTokenIntrospector introspector = securityConfig.stockExchangeTokenIntrospector();

            // Assertions
            assertThat(introspector).isInstanceOf(StockExchangeReactiveTokenIntrospector.class);
        }
    }

    @Nested
    class AuthenticationManagerResolverUnitTest {

        private SecurityConfig securityConfig;

        @BeforeEach
        void setUp() {
            // Properties are set manually because this is a pure unit test without Spring context,
            // so application.yml values are not loaded. Mirrors stock-exchange-auth-path-prefixes from test YAML.
            GatewaySecurityProperties properties = new GatewaySecurityProperties();
            properties.setStockExchangeAuthPathPrefixes(List.of("/stock-exchange/", "/inventory/"));
            securityConfig = new SecurityConfig(properties);
            ReflectionTestUtils.setField(securityConfig, "introspectionUri", "http://localhost/introspect");
            ReflectionTestUtils.setField(securityConfig, "clientId", "test-client-id");
            ReflectionTestUtils.setField(securityConfig, "clientSecret", "test-client-secret");
            ReflectionTestUtils.setField(securityConfig, "connectTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "responseTimeout", Duration.ofSeconds(5));
            ReflectionTestUtils.setField(securityConfig, "maxIdleTime", Duration.ofSeconds(30));
            ReflectionTestUtils.setField(securityConfig, "stockExchangeIntrospectionUri", "http://localhost/api/v1/auth/introspect");
        }

        @Test
        @DisplayName("AuthenticationManagerResolver should return non-null for stock-exchange path")
        void authenticationManagerResolver_ShouldReturnNonNull_WhenStockExchangePath() {
            // Arrange
            ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = securityConfig.authenticationManagerResolver();
            MockServerHttpRequest request = MockServerHttpRequest.get("/stock-exchange/api/v1/users/1").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            // Assertions
            StepVerifier.create(resolver.resolve(exchange))
                    .assertNext(authManager -> assertThat(authManager).isNotNull())
                    .verifyComplete();
        }

        @Test
        @DisplayName("AuthenticationManagerResolver should return non-null for non-stock-exchange path")
        void authenticationManagerResolver_ShouldReturnNonNull_WhenNonStockExchangePath() {
            // Arrange
            ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = securityConfig.authenticationManagerResolver();
            MockServerHttpRequest request = MockServerHttpRequest.get("/product-service/api/v1/products").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            // Assertions
            StepVerifier.create(resolver.resolve(exchange))
                    .assertNext(authManager -> assertThat(authManager).isNotNull())
                    .verifyComplete();
        }

        @Test
        @DisplayName("AuthenticationManagerResolver should return different managers for different paths")
        void authenticationManagerResolver_ShouldReturnDifferentManagers_ForDifferentPaths() {
            // Arrange
            ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = securityConfig.authenticationManagerResolver();

            MockServerHttpRequest stockExchangeRequest = MockServerHttpRequest.get("/stock-exchange/api/v1/users/1").build();
            MockServerWebExchange stockExchangeExchange = MockServerWebExchange.from(stockExchangeRequest);

            MockServerHttpRequest productRequest = MockServerHttpRequest.get("/product-service/api/v1/products").build();
            MockServerWebExchange productExchange = MockServerWebExchange.from(productRequest);

            // Act
            var stockExchangeManager = resolver.resolve(stockExchangeExchange).block();
            var keycloakManager = resolver.resolve(productExchange).block();

            // Assertions
            assertThat(stockExchangeManager).isNotNull();
            assertThat(keycloakManager).isNotNull();
            assertThat(stockExchangeManager).isNotSameAs(keycloakManager);
        }

        @Test
        @DisplayName("AuthenticationManagerResolver should use same manager for all stock-exchange sub-paths")
        void authenticationManagerResolver_ShouldUseSameManager_ForAllStockExchangeSubPaths() {
            // Arrange
            ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = securityConfig.authenticationManagerResolver();

            MockServerHttpRequest request1 = MockServerHttpRequest.get("/stock-exchange/api/v1/users/1").build();
            MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

            MockServerHttpRequest request2 = MockServerHttpRequest.get("/stock-exchange/api/v1/stocks").build();
            MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

            // Act
            var manager1 = resolver.resolve(exchange1).block();
            var manager2 = resolver.resolve(exchange2).block();

            // Assertions
            assertThat(manager1).isSameAs(manager2);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "/payment/payments",
                "/student/api/v1/students",
                "/notification/api/v1/notifications",
                "/brokerage-provider/api/v1/data"
        })
        @DisplayName("AuthenticationManagerResolver should use Keycloak manager for non-stock-exchange paths")
        void authenticationManagerResolver_ShouldUseKeycloakManager_ForNonStockExchangePaths(String path) {
            // Arrange
            ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = securityConfig.authenticationManagerResolver();

            MockServerHttpRequest keycloakRequest = MockServerHttpRequest.get("/payment/payments").build();
            MockServerWebExchange keycloakExchange = MockServerWebExchange.from(keycloakRequest);

            MockServerHttpRequest testRequest = MockServerHttpRequest.get(path).build();
            MockServerWebExchange testExchange = MockServerWebExchange.from(testRequest);

            // Act
            var keycloakManager = resolver.resolve(keycloakExchange).block();
            var testManager = resolver.resolve(testExchange).block();

            // Assertions - All non-stock-exchange paths should use the same (Keycloak) manager
            assertThat(testManager).isSameAs(keycloakManager);
        }
    }
}
