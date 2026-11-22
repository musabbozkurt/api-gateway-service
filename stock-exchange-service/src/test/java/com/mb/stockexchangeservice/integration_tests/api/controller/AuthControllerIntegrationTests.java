package com.mb.stockexchangeservice.integration_tests.api.controller;

import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.config.TestRedisConfiguration;
import com.mb.stockexchangeservice.exception.ErrorResponse;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class AuthControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== TestRestTemplate Tests ====================

    @Test
    void authenticateUser_ShouldReturnJwtToken_WhenCredentialsAreValid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword("test1234");

        // Act
        EntityExchangeResult<JwtResponse> response = exchange(HttpMethod.POST, "/api/v1/auth/signin", request, JwtResponse.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());

        JwtResponse jwtResponse = response.getResponseBody();
        assertNotNull(jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType());
        assertEquals(request.getUsername(), jwtResponse.getUsername());
        assertTrue(jwtUtils.validateJwtToken(jwtResponse.getToken()));
        assertEquals(request.getUsername(), jwtUtils.getUserNameFromJwtToken(jwtResponse.getToken()));
    }

    @Test
    void authenticateUser_ShouldReturnForbidden_WhenCredentialsAreInvalid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("example_username");
        request.setPassword("example_password");

        // Act
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.POST, "/api/v1/auth/signin", request, ErrorResponse.class);

        // Assertions
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
        assertNotNull(response.getResponseBody());
        assertEquals(StockExchangeServiceErrorCode.BAD_CREDENTIALS.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    void authenticateUser_ShouldReturnRoles_WhenCredentialsAreValid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword("test1234");

        // Act
        EntityExchangeResult<JwtResponse> response = exchange(HttpMethod.POST, "/api/v1/auth/signin", request, JwtResponse.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
        assertNotNull(response.getResponseBody().getRoles());
        assertFalse(response.getResponseBody().getRoles().isEmpty());
    }

    // ==================== RestTestClient Tests ====================

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnJwtToken_WhenCredentialsAreValid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword("test1234");

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtResponse.class)
                .value(jwtResponse -> {
                    assertNotNull(jwtResponse);
                    assertNotNull(jwtResponse.getToken());
                    assertEquals("Bearer", jwtResponse.getType());
                    assertEquals(request.getUsername(), jwtResponse.getUsername());
                    assertTrue(jwtUtils.validateJwtToken(jwtResponse.getToken()));
                    assertEquals(request.getUsername(), jwtUtils.getUserNameFromJwtToken(jwtResponse.getToken()));
                });
    }

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnForbidden_WhenCredentialsAreInvalid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("example_username");
        request.setPassword("example_password");

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(errorResponse -> {
                    assertNotNull(errorResponse);
                    assertEquals(StockExchangeServiceErrorCode.BAD_CREDENTIALS.getCode(), errorResponse.getErrorCode());
                });
    }

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnRoles_WhenCredentialsAreValid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword("test1234");

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtResponse.class)
                .value(jwtResponse -> {
                    assertNotNull(jwtResponse);
                    assertNotNull(jwtResponse.getRoles());
                    assertFalse(jwtResponse.getRoles().isEmpty());
                });
    }

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnBadRequest_WhenRequestBodyIsEmpty() {
        // Arrange
        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnBadRequest_WhenUsernameIsNull() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername(null);
        request.setPassword("test1234");

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void authenticateUserWithRestTestClient_ShouldReturnBadRequest_WhenPasswordIsNull() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword(null);

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private <T> EntityExchangeResult<T> exchange(HttpMethod method, String uri, Object body, Class<T> type) {
        RestTestClient.RequestBodySpec spec = restTestClient.method(method).uri(uri).accept(MediaType.APPLICATION_JSON);
        RestTestClient.RequestHeadersSpec<?> headersSpec = spec;
        if (body != null) {
            headersSpec = spec.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return headersSpec.exchange().returnResult(type);
    }
}
