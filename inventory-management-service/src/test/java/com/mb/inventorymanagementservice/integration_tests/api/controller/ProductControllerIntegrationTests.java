package com.mb.inventorymanagementservice.integration_tests.api.controller;

import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.config.TestRedisConfiguration;
import com.mb.inventorymanagementservice.exception.ErrorResponse;
import com.mb.inventorymanagementservice.exception.InventoryManagementServiceErrorCode;
import com.mb.inventorymanagementservice.mapper.ProductMapper;
import com.mb.inventorymanagementservice.service.ProductService;
import com.mb.inventorymanagementservice.utils.JwtUtils;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class ProductControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @BeforeAll
    void setUp() {
        // Generate JWT token directly using JwtUtils (tokens are compatible with stock-exchange-service)
        UserDetails userDetails = new User("admin_user", "", List.of(
                new SimpleGrantedAuthority("ADMIN"),
                new SimpleGrantedAuthority("GET_PRODUCT"),
                new SimpleGrantedAuthority("CREATE_PRODUCT"),
                new SimpleGrantedAuthority("UPDATE_PRODUCT"),
                new SimpleGrantedAuthority("DELETE_PRODUCT"),
                new SimpleGrantedAuthority("ADD_PRODUCT"),
                new SimpleGrantedAuthority("REMOVE_PRODUCT"),
                new SimpleGrantedAuthority("GET_CATEGORY"),
                new SimpleGrantedAuthority("CREATE_CATEGORY"),
                new SimpleGrantedAuthority("UPDATE_CATEGORY"),
                new SimpleGrantedAuthority("DELETE_CATEGORY")
        ));
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String token = jwtUtils.generateJwtToken(authentication);

        restTestClient = restTestClient.mutate().defaultHeader("Authorization", "Bearer " + token).build();
    }

    private <T> EntityExchangeResult<T> exchange(HttpMethod method, String uri, Object body, Class<T> type) {
        return request(method, uri, body).exchange().returnResult(type);
    }

    private RestTestClient.RequestBodySpec request(HttpMethod method, String uri, Object body) {
        RestTestClient.RequestBodySpec spec = restTestClient.method(method).uri(uri).accept(MediaType.APPLICATION_JSON);
        if (body != null) {
            spec = spec.contentType(MediaType.APPLICATION_JSON);
            spec.body(body);
        }
        return spec;
    }

    @Test
    @Order(value = 1)
    void testConnectionToDatabase() {
        Assertions.assertNotNull(productService);
        Assertions.assertNotNull(productMapper);
    }

    @Test
    @Order(value = 2)
    void testGetAllProducts() {

        EntityExchangeResult<String> response = exchange(HttpMethod.GET, "/products/", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    @Order(value = 3)
    void testGetProductById() {
        EntityExchangeResult<ApiProductResponse> response = exchange(HttpMethod.GET, "/products/Novel", null, ApiProductResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals("Novel", response.getResponseBody().getName());
    }

    @Test
    @Order(value = 4)
    void testGetProductById_ShouldFail_WhenProductIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.GET, "/products/Novell", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testCreateProduct() {
        ApiProductRequest apiProductRequest = getApiProductRequest();

        EntityExchangeResult<ApiProductResponse> response = exchange(HttpMethod.POST, "/products/", apiProductRequest, ApiProductResponse.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        Assertions.assertEquals(apiProductRequest.getName(), response.getResponseBody().getName());
        Assertions.assertEquals(apiProductRequest.getDescription(), response.getResponseBody().getDescription());
        Assertions.assertEquals(apiProductRequest.getPrice(), response.getResponseBody().getPrice());
    }

    @Test
    @Order(value = 6)
    void testCreateProduct_ShouldFail_WhenProductCodeIsAlreadyExists() {
        ApiProductRequest apiProductRequest = getApiProductRequest2();

        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.POST, "/products/", apiProductRequest, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.ALREADY_EXISTS.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteProduct() {
        EntityExchangeResult<String> response = exchange(HttpMethod.DELETE, "/products/6", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("Product deleted successfully.", response.getResponseBody());
    }

    @Test
    @Order(value = 8)
    void testDeleteProduct_ShouldFail_WhenProductIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/products/1", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 9)
    void testGetQuantityByProductCode() {
        EntityExchangeResult<Integer> response = exchange(HttpMethod.GET, "/products/IPHONE_13/quantity", null, Integer.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(10, response.getResponseBody());
    }

    @Test
    @Order(value = 10)
    void testGetQuantityByProductCode_ShouldReturnNull_WhenProductCodeIsNotFound() {
        EntityExchangeResult<Integer> response = exchange(HttpMethod.GET, "/products/Novelll/quantity", null, Integer.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNull(response.getResponseBody());
    }
}
