package com.mb.stockexchangeservice.integration_tests.api.controller;

import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.ApiStockResponse;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.config.TestRedisConfiguration;
import com.mb.stockexchangeservice.exception.ErrorResponse;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.mapper.StockMapper;
import com.mb.stockexchangeservice.service.StockService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class StockControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMapper stockMapper;

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
    void testConnectionToDatabase() {
        Assertions.assertNotNull(stockService);
        Assertions.assertNotNull(stockMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllStocks() {
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, "/api/v1/stock/", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    @Order(value = 3)
    void testGetStockById() {
        EntityExchangeResult<ApiStockResponse> response = exchange(HttpMethod.GET, "/api/v1/stock/AAPL", null, ApiStockResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals("AAPL", response.getResponseBody().getName());
    }

    @Test
    @Order(value = 4)
    void testGetStockById_ShouldFail_WhenStockIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.GET, "/api/v1/stock/AAPLL", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testCreateStock() {
        ApiStockRequest apiStockRequest = getApiStockRequest();

        EntityExchangeResult<ApiStockResponse> response = exchange(HttpMethod.POST, "/api/v1/stock/", apiStockRequest, ApiStockResponse.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(apiStockRequest.getName(), response.getResponseBody().getName());
        Assertions.assertEquals(apiStockRequest.getDescription(), response.getResponseBody().getDescription());
        Assertions.assertEquals(apiStockRequest.getCurrentPrice(), response.getResponseBody().getCurrentPrice());
    }

    @Test
    @Order(value = 6)
    void testCreateStock_ShouldFail_WhenProductCodeIsAlreadyExists() {
        ApiStockRequest apiStockRequest = getApiStockRequest2();

        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.POST, "/api/v1/stock/", apiStockRequest, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.ALREADY_EXISTS.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteStock() {
        EntityExchangeResult<String> response = exchange(HttpMethod.DELETE, "/api/v1/stock/3", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("Stock deleted successfully.", response.getResponseBody());
    }

    @Test
    @Order(value = 8)
    void testDeleteStock_ShouldFail_WhenStockIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/api/v1/stock/1", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(StockExchangeServiceErrorCode.STOCK_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
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
