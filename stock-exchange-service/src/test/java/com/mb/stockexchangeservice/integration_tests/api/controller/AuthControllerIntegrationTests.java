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
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureRestTestClient
@AutoConfigureTestRestTemplate
@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class AuthControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

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

        HttpEntity<ApiUserAuthRequest> entity = createHttpEntity(request);

        // Act
        ResponseEntity<JwtResponse> response = testRestTemplate.exchange("/api/v1/auth/signin", HttpMethod.POST, entity, JwtResponse.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        JwtResponse jwtResponse = response.getBody();
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

        HttpEntity<ApiUserAuthRequest> entity = createHttpEntity(request);

        // Act
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/api/v1/auth/signin", HttpMethod.POST, entity, ErrorResponse.class);

        // Assertions
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StockExchangeServiceErrorCode.BAD_CREDENTIALS.getCode(), response.getBody().getErrorCode());
    }

    @Test
    void authenticateUser_ShouldReturnRoles_WhenCredentialsAreValid() {
        // Arrange
        ApiUserAuthRequest request = new ApiUserAuthRequest();
        request.setUsername("admin_user");
        request.setPassword("test1234");

        HttpEntity<ApiUserAuthRequest> entity = createHttpEntity(request);

        // Act
        ResponseEntity<JwtResponse> response = testRestTemplate.exchange("/api/v1/auth/signin", HttpMethod.POST, entity, JwtResponse.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRoles());
        assertFalse(response.getBody().getRoles().isEmpty());
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

    private HttpEntity<ApiUserAuthRequest> createHttpEntity(ApiUserAuthRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}
