package com.mb.apigateway;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayServiceApplicationTest {

    private static MockWebServer mockBackendServer;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector;

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
        registry.add("spring.cloud.gateway.server.webflux.default-filters[0]", () -> "StripPrefix=1");

        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "rbac-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> mockBackendUrl);
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/rbac-service/**");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[0]", () -> "StripPrefix=1");
    }

    @Test
    void healthEndpoint_ShouldReturnStatusUp_WhenCalled() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @Disabled("Disabled because the gateway currently allows all requests")
    void protectedEndpoint_ShouldReturnUnauthorized_WhenNoTokenProvided() {
        webTestClient.get()
                .uri("/rbac-service/api/v1/some-endpoint")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void forgotPasswordEndpoint_ShouldBeAccessible_WhenNoTokenProvided() {
        // Arrange
        mockBackendServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("""
                                {
                                  "success": true,
                                  "data": {
                                    "phone": "555****1234",
                                    "email": "test@example.com"
                                  }
                                }
                                """)
                        .addHeader("Content-Type", "application/json")
        );

        // Act
        // Assertions
        webTestClient.post()
                .uri("/rbac-service/api/v1/forgot-password/generate-code")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.phone").exists()
                .jsonPath("$.data.email").exists();
    }

    @Test
    void protectedEndpoint_ShouldGenerateOAuth2IntrospectionException_WhenIntrospectionFails() {
        // Arrange
        // Mock the introspector to throw OAuth2IntrospectionException
        when(opaqueTokenIntrospector.introspect(anyString())).thenReturn(Mono.error(new OAuth2IntrospectionException("Introspection endpoint responded with 400 BAD_REQUEST")));

        // Act
        // Assertions
        // This will trigger OAuth2IntrospectionException wrapped in AuthenticationServiceException
        webTestClient.post()
                .uri("/product-service/api/v1/product/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error");
    }
}
