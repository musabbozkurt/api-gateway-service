package com.mb.stockexchangeservice.integration_tests.api.controller;

import com.mb.stockexchangeservice.api.request.ApiStockExchangeRequest;
import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.ApiStockExchangeResponse;
import com.mb.stockexchangeservice.api.response.ApiStockResponse;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.config.TestRedisConfiguration;
import com.mb.stockexchangeservice.exception.ErrorResponse;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class StockExchangeControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

    @BeforeAll
    void setUp() {
        // Arrange
        ApiUserAuthRequest apiUserAuthRequest = new ApiUserAuthRequest();
        apiUserAuthRequest.setUsername("admin_user");
        apiUserAuthRequest.setPassword("test1234");

        // Act
        EntityExchangeResult<JwtResponse> response = exchange(HttpMethod.POST, "/api/v1/auth/signin", apiUserAuthRequest, JwtResponse.class);

        JwtResponse jwtResponse = response.getResponseBody();

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(jwtResponse);
        String token = jwtResponse.getToken();
        assertNotNull(token);

        restTestClient = restTestClient.mutate().defaultHeader("Authorization", "Bearer ".concat(token)).build();
    }

    @Test
    @Order(value = 1)
    void testCreateStockExchange() {
        ApiStockExchangeRequest request = new ApiStockExchangeRequest();
        request.setName("Toronto Stock Exchange");
        request.setDescription("Toronto Stock Exchange Description");

        EntityExchangeResult<ApiStockExchangeResponse> response = exchange(HttpMethod.POST, "/api/v1/stock-exchange/", request, ApiStockExchangeResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getResponseBody());
        assertNotNull(response.getResponseBody().getId());
        assertEquals(request.getName(), response.getResponseBody().getName());
        assertEquals(request.getDescription(), response.getResponseBody().getDescription());
    }

    @Test
    @Order(value = 2)
    void testAddStockToStockExchange() {
        ApiStockRequest stockRequest = new ApiStockRequest();
        stockRequest.setName("AAPL");
        stockRequest.setDescription("APPLE Stock Description");
        stockRequest.setCurrentPrice(BigDecimal.valueOf(100.0));

        EntityExchangeResult<ApiStockExchangeResponse> response = exchange(HttpMethod.POST, "/api/v1/stock-exchange/New York Stock Exchange", stockRequest, ApiStockExchangeResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 3)
    void testAddStockToStockExchange_ShouldFail_WhenStockIsNotFound() {
        ApiStockRequest stockRequest = new ApiStockRequest();
        stockRequest.setName("AAPLE");
        stockRequest.setDescription("APPLE Description");
        stockRequest.setCurrentPrice(BigDecimal.valueOf(100.0));

        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.POST, "/api/v1/stock-exchange/New York Stock Exchange", stockRequest, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 4)
    void testGetStocksByStockExchangeName() {
        EntityExchangeResult<List<ApiStockResponse>> response = exchange(HttpMethod.GET, "/api/v1/stock-exchange/New York Stock Exchange", null, new ParameterizedTypeReference<>() {
        });

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 5)
    void testGetStocksByStockExchangeName_ShouldFail_WhenStockExchangeIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.GET, "/api/v1/stock-exchange/Bombay Stock Exchange", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_EXCHANGE_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 6)
    void testDeleteStockFromStockExchange() {
        EntityExchangeResult<ApiStockExchangeResponse> response = exchange(HttpMethod.DELETE, "/api/v1/stock-exchange/New York Stock Exchange/AAPL", null, ApiStockExchangeResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 7)
    void testDeleteStockFromStockExchange_ShouldFail_WhenStockExchangeIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/api/v1/stock-exchange/Bombay Stock Exchange/APPL", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_EXCHANGE_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 8)
    void testDeleteStockFromStockExchange_ShouldFail_WhenStockIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/api/v1/stock-exchange/New York Stock Exchange/APPLE", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    private <T> EntityExchangeResult<T> exchange(HttpMethod method, String uri, Object body, Class<T> type) {
        return buildExchange(method, uri, body).returnResult(type);
    }

    private <T> EntityExchangeResult<T> exchange(HttpMethod method, String uri, Object body, ParameterizedTypeReference<T> type) {
        return buildExchange(method, uri, body).returnResult(type);
    }

    private RestTestClient.ResponseSpec buildExchange(HttpMethod method, String uri, Object body) {
        RestTestClient.RequestBodySpec spec = restTestClient.method(method).uri(uri).accept(MediaType.APPLICATION_JSON);
        RestTestClient.RequestHeadersSpec<?> headersSpec = spec;
        if (body != null) {
            headersSpec = spec.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return headersSpec.exchange();
    }
}
