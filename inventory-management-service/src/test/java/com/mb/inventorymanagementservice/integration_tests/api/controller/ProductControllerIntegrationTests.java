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
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestRestTemplate
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class ProductControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

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

        // Create RestTemplate with custom header interceptor
        testRestTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer ".concat(token));
            return execution.execute(request, body);
        }));
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
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = testRestTemplate.exchange("/products/", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(value = 3)
    void testGetProductById() {
        ResponseEntity<ApiProductResponse> response = testRestTemplate.getForEntity("/products/Novel", ApiProductResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals("Novel", response.getBody().getName());
    }

    @Test
    @Order(value = 4)
    void testGetProductById_ShouldFail_WhenProductIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("/products/Novell", ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 5)
    void testCreateProduct() {
        ApiProductRequest apiProductRequest = getApiProductRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ApiProductResponse> response = testRestTemplate.exchange("/products/", HttpMethod.POST, new HttpEntity<>(apiProductRequest, headers), ApiProductResponse.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(apiProductRequest.getName(), response.getBody().getName());
        Assertions.assertEquals(apiProductRequest.getDescription(), response.getBody().getDescription());
        Assertions.assertEquals(apiProductRequest.getPrice(), response.getBody().getPrice());
    }

    @Test
    @Order(value = 6)
    void testCreateProduct_ShouldFail_WhenProductCodeIsAlreadyExists() {
        ApiProductRequest apiProductRequest = getApiProductRequest2();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/products/", HttpMethod.POST, new HttpEntity<>(apiProductRequest, headers), ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.ALREADY_EXISTS.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteProduct() {
        ResponseEntity<String> response = testRestTemplate.exchange("/products/6", HttpMethod.DELETE, null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Product deleted successfully.", response.getBody());
    }

    @Test
    @Order(value = 8)
    void testDeleteProduct_ShouldFail_WhenProductIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/products/1", HttpMethod.DELETE, null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 9)
    void testGetQuantityByProductCode() {
        ResponseEntity<Integer> response = testRestTemplate.getForEntity("/products/IPHONE_13/quantity", Integer.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(10, response.getBody());
    }

    @Test
    @Order(value = 10)
    void testGetQuantityByProductCode_ShouldReturnNull_WhenProductCodeIsNotFound() {
        ResponseEntity<Integer> response = testRestTemplate.getForEntity("/products/Novelll/quantity", Integer.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }
}
