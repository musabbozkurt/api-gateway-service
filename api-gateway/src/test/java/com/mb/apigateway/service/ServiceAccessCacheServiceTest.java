package com.mb.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.apigateway.constant.GatewayServiceConstants;
import com.mb.apigateway.service.impl.ServiceAccessCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccessCacheServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ServiceAccessCacheServiceImpl serviceAccessCacheService;

    @Mock
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    private static Stream<Arguments> hasAccessScenarios() {
        return Stream.of(
                // full access exists for service with CLIENT access type
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", true),

                // specific api access matches with CLIENT access type
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", true),

                // api matches and method is null
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": null, "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "POST", "USER", true),

                // api matches and method is empty
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "", "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "POST", "USER", true),

                // api matches and method is blank
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "  ", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "DELETE", "CLIENT", true),

                // full access with empty api and method
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "", "method": "", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", true),

                // api is null but method has text
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": null, "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // no entries for service
                Arguments.of("""
                                [
                                    {"serviceName": "swagger-application", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // api does not match
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/categories", "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // method does not match
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "DELETE", "CLIENT", false),

                // method matches case-insensitive
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "get", "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "USER", true),

                // multiple entries and one matches
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/categories", "method": "GET", "accessType": "CLIENT"},
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "CLIENT"},
                                    {"serviceName": "swagger-application", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", true),

                // access type mismatch - entry is CLIENT but request is USER
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "USER", false),

                // access type mismatch - entry is USER but request is CLIENT
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // request api is null — should match full access entry only
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", null, "GET", "CLIENT", true),

                // request api is null — should NOT match specific api entry
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", null, "GET", "CLIENT", false),

                // request api is blank — should NOT match specific api entry
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": "/api/v1/products", "method": "GET", "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "", "GET", "CLIENT", false),

                // request api is blank — should match full access entry
                Arguments.of("""
                                [
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "", "GET", "CLIENT", true),

                // client inactive marker for CLIENT access type — should deny
                Arguments.of("""
                                [
                                    {"serviceName": null, "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // client inactive marker for USER access type — should deny
                Arguments.of("""
                                [
                                    {"serviceName": null, "api": null, "method": null, "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "USER", false),

                // client inactive marker for CLIENT but request is USER — should not affect USER access
                Arguments.of("""
                                [
                                    {"serviceName": null, "api": null, "method": null, "accessType": "CLIENT"},
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "USER", true),

                // client inactive marker takes precedence over service access entries
                Arguments.of("""
                                [
                                    {"serviceName": null, "api": null, "method": null, "accessType": "CLIENT"},
                                    {"serviceName": "product-service", "api": null, "method": null, "accessType": "CLIENT"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "CLIENT", false),

                // both access types inactive
                Arguments.of("""
                                [
                                    {"serviceName": null, "api": null, "method": null, "accessType": "CLIENT"},
                                    {"serviceName": null, "api": null, "method": null, "accessType": "USER"}
                                ]
                                """,
                        "product-service", "/api/v1/products", "GET", "USER", false)
        );
    }

    @BeforeEach
    void setUp() {
        serviceAccessCacheService = new ServiceAccessCacheServiceImpl(reactiveStringRedisTemplate, objectMapper);
    }

    @ParameterizedTest
    @MethodSource("hasAccessScenarios")
    void hasAccess_ShouldReturnExpectedResult_WhenCacheEntryExists(String json, String serviceName, String api, String method, String accessType, boolean expected) {
        // Arrange
        when(reactiveStringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(GatewayServiceConstants.SERVICE_ACCESS_KEY_PREFIX + "test-client")).thenReturn(Mono.just(json));

        // Act
        Mono<Boolean> result = serviceAccessCacheService.hasAccess("test-client", serviceName, api, method, accessType);

        // Assertions
        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenNoCacheEntryExists() {
        // Arrange
        when(reactiveStringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(GatewayServiceConstants.SERVICE_ACCESS_KEY_PREFIX + "test-client")).thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = serviceAccessCacheService.hasAccess("test-client", "product-service", "/api/v1/products", "GET", "CLIENT");

        // Assertions
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenClientIdIsNull() {
        // Arrange
        // Act
        Mono<Boolean> result = serviceAccessCacheService.hasAccess(null, "product-service", "/api/v1/products", "GET", "CLIENT");

        // Assertions
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenServiceNameIsNull() {
        // Arrange
        // Act
        Mono<Boolean> result = serviceAccessCacheService.hasAccess("test-client", null, "/api/v1/products", "GET", "CLIENT");

        // Assertions
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenJsonIsInvalid() {
        // Arrange
        String invalidJson = "invalid-json";

        when(reactiveStringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(GatewayServiceConstants.SERVICE_ACCESS_KEY_PREFIX + "test-client")).thenReturn(Mono.just(invalidJson));

        // Act
        Mono<Boolean> result = serviceAccessCacheService.hasAccess("test-client", "product-service", "/api/v1/products", "GET", "CLIENT");

        // Assertions
        StepVerifier.create(result)
                .expectNext(true) // Fail open on parse errors
                .verifyComplete();
    }
}
