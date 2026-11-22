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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

@EnableTestBinder
@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestSecurityConfig.class)
class UserControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

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
        String response = exchange(HttpMethod.GET, "/api/v1/users/", null, String.class).getResponseBody();

        Assertions.assertNotNull(response);
    }

    @Test
    @Order(value = 3)
    void testCreateUser() {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        ApiUserResponse response = exchange(HttpMethod.POST, "/api/v1/users/", apiUserRequest, ApiUserResponse.class).getResponseBody();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(apiUserRequest.getFirstName(), response.getFirstName());
        Assertions.assertEquals(apiUserRequest.getLastName(), response.getLastName());
        Assertions.assertEquals(apiUserRequest.getEmail(), response.getEmail());
    }

    @Test
    @Order(value = 4)
    void testCreateUser_ShouldFail_WhenPhoneNumberOrEmailIsAlreadyExists() {
        ApiUserRequest apiUserRequest = getApiUserRequest2();
        ErrorResponse exception = exchange(HttpMethod.POST, "/api/v1/users/", apiUserRequest, ErrorResponse.class).getResponseBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.UNEXPECTED_ERROR.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testGetUserById() {
        ApiUserResponse response = exchange(HttpMethod.GET, "/api/v1/users/1", null, ApiUserResponse.class).getResponseBody();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getId());
        Assertions.assertNotNull(response.getUsername());
        Assertions.assertNotNull(response.getEmail());
    }

    @Test
    @Order(value = 6)
    void testGetUserById_ShouldFail_WhenUserIsNotFound() {
        ErrorResponse exception = exchange(HttpMethod.GET, "/api/v1/users/0", null, ErrorResponse.class).getResponseBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testUpdateUserById() {
        ApiUserRequest apiUserRequest = getApiUserRequest3();
        ApiUserResponse response = exchange(HttpMethod.PUT, "/api/v1/users/1", apiUserRequest, ApiUserResponse.class).getResponseBody();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(apiUserRequest.getEmail(), response.getEmail());
    }

    @Test
    @Order(value = 8)
    void testUpdateUserById_ShouldFail_WhenUserIsNotFound() {
        ApiUserRequest apiUserRequest = getApiUserRequest();
        ErrorResponse exception = exchange(HttpMethod.PUT, "/api/v1/users/0", apiUserRequest, ErrorResponse.class).getResponseBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    @Order(value = 9)
    @Disabled("Disabled to preserve test data integrity.")
    void testDeleteUserById() {
        String response = exchange(HttpMethod.DELETE, "/api/v1/users/13", null, String.class).getResponseBody();

        Assertions.assertEquals("User deleted successfully.", response);
    }

    @Test
    @Order(value = 10)
    void testDeleteUserById_ShouldFail_WhenUserIsNotFound() {
        ErrorResponse exception = exchange(HttpMethod.DELETE, "/api/v1/users/0", null, ErrorResponse.class).getResponseBody();

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(StockExchangeServiceErrorCode.USER_NOT_FOUND.getCode(), exception.getErrorCode());
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
