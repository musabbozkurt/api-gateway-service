package com.mb.stockexchangeservice.integration_tests.api.controller;

import com.mb.stockexchangeservice.api.request.ApiUserRequest;
import com.mb.stockexchangeservice.api.response.ApiUserResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.config.TestSecurityConfig;
import com.mb.stockexchangeservice.exception.ErrorResponse;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.mapper.UserMapper;
import com.mb.stockexchangeservice.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

@EnableTestBinder
@AutoConfigureTestRestTemplate
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestSecurityConfig.class)
class UserControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @Order(value = 1)
    void testServiceConnection() {
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(userMapper);
    }

    @Test
    @Order(value = 2)
    void testGetUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String response = testRestTemplate.exchange("/api/v1/users/", HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();

        Assertions.assertNotNull(response);
    }

    @Test
    @Order(value = 3)
    void testCreateUser() {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ApiUserResponse response = testRestTemplate.postForObject("/api/v1/users/", new HttpEntity<>(apiUserRequest, headers), ApiUserResponse.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(apiUserRequest.getFirstName(), response.getFirstName());
        Assertions.assertEquals(apiUserRequest.getLastName(), response.getLastName());
        Assertions.assertEquals(apiUserRequest.getEmail(), response.getEmail());
    }

    @Test
    @Order(value = 4)
    void testCreateUser_ShouldFail_WhenPhoneNumberOrEmailIsAlreadyExists() {
        ApiUserRequest apiUserRequest = getApiUserRequest2();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse exception = testRestTemplate.postForObject("/api/v1/users/", new HttpEntity<>(apiUserRequest, headers), ErrorResponse.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.UNEXPECTED_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testGetUserById() {
        ApiUserResponse response = testRestTemplate.getForObject("/api/v1/users/1", ApiUserResponse.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getId());
        Assertions.assertNotNull(response.getUsername());
        Assertions.assertNotNull(response.getEmail());
    }

    @Test
    @Order(value = 6)
    void testGetUserById_ShouldFail_WhenUserIsNotFound() {
        ErrorResponse exception = testRestTemplate.getForObject("/api/v1/users/0", ErrorResponse.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testUpdateUserById() {
        ApiUserRequest apiUserRequest = getApiUserRequest3();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ApiUserResponse response = testRestTemplate.exchange("/api/v1/users/1", HttpMethod.PUT, new HttpEntity<>(apiUserRequest, headers), ApiUserResponse.class).getBody();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(apiUserRequest.getEmail(), response.getEmail());
    }

    @Test
    @Order(value = 8)
    void testUpdateUserById_ShouldFail_WhenUserIsNotFound() {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse exception = testRestTemplate.exchange("/api/v1/users/0", HttpMethod.PUT, new HttpEntity<>(apiUserRequest, headers), ErrorResponse.class).getBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 9)
    @Disabled("Disabled to preserve test data integrity.")
    void testDeleteUserById() {
        String response = testRestTemplate.exchange("/api/v1/users/13", HttpMethod.DELETE, null, String.class).getBody();

        Assertions.assertEquals("User deleted successfully.", response);
    }

    @Test
    @Order(value = 10)
    void testDeleteUserById_ShouldFail_WhenUserIsNotFound() {
        ErrorResponse exception = testRestTemplate.exchange("/api/v1/users/0", HttpMethod.DELETE, null, ErrorResponse.class).getBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
    }
}
