package com.mb.inventorymanagementservice.integration_tests.api.controller;

import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.response.ApiCategoryResponse;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.config.TestRedisConfiguration;
import com.mb.inventorymanagementservice.exception.ErrorResponse;
import com.mb.inventorymanagementservice.exception.InventoryManagementServiceErrorCode;
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
import org.springframework.core.ParameterizedTypeReference;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestRestTemplate
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class CategoryControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

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
    void testCreateCategory() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ApiCategoryRequest request = new ApiCategoryRequest();
        request.setName("Book Category");
        request.setDescription("Book Category Description");

        ResponseEntity<ApiCategoryResponse> response = testRestTemplate.exchange("/categories/", HttpMethod.POST, new HttpEntity<>(request, headers), ApiCategoryResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(request.getName(), response.getBody().getName());
        assertEquals(request.getDescription(), response.getBody().getDescription());
    }

    @Test
    @Order(value = 2)
    void testGetAllCategories() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = testRestTemplate.exchange("/categories/", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(value = 3)
    void testGetCategoryByName() {
        ResponseEntity<ApiCategoryResponse> response = testRestTemplate.getForEntity("/categories/Electronics Category", ApiCategoryResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals("Electronics Category", response.getBody().getName());
    }

    @Test
    @Order(value = 4)
    void testGetCategoryByName_ShouldFail_WhenCategoryIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("/categories/Book Categoryy", ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 6)
    void testCreateCategory_ShouldFail_WhenCategoryCodeIsAlreadyExists() {
        ApiCategoryRequest apiCategoryRequest = getApiCategoryRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/categories/", HttpMethod.POST, new HttpEntity<>(apiCategoryRequest, headers), ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.ALREADY_EXISTS.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteCategory() {
        ResponseEntity<String> response = testRestTemplate.exchange("/categories/2", HttpMethod.DELETE, null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Category deleted successfully.", response.getBody());
    }

    @Test
    @Order(value = 8)
    void testDeleteCategory_ShouldFail_WhenCategoryIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/categories/15", HttpMethod.DELETE, null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 9)
    void testAddProductToCategory() {
        ResponseEntity<ApiCategoryResponse> response = testRestTemplate.exchange("/categories/Book Category/Art", HttpMethod.PUT, null, ApiCategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(value = 10)
    void testAddProductToCategory_ShouldFail_WhenProductIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/categories/Beauty Category/Perfumes", HttpMethod.PUT, null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 11)
    void testGetProductsByCategory() {
        ResponseEntity<List<ApiProductResponse>> response = testRestTemplate.exchange("/categories/Beauty Category/products", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(value = 12)
    void testGetProductsByCategoryName_ShouldFail_WhenCategoryIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("/categories/Automotive Category/products", ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 13)
    void testDeleteProductFromCategory() {
        ResponseEntity<ApiCategoryResponse> response = testRestTemplate.exchange("/categories/Book Category/Art", HttpMethod.DELETE, null, ApiCategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(value = 14)
    void testDeleteProductFromCategory_ShouldFail_WhenCategoryIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/categories/Automotive Category/BMW", HttpMethod.DELETE, null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }

    @Test
    @Order(value = 15)
    void testDeleteProductFromCategory_ShouldFail_WhenProductIsNotFound() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange("/categories/Beauty Category/Perfumes", HttpMethod.DELETE, null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getBody().getErrorCode());
    }
}
