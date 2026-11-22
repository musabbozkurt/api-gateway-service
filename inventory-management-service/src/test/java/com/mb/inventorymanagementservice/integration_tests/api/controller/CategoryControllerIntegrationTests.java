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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureRestTestClient
@ActiveProfiles("test-containers")
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestRedisConfiguration.class)
class CategoryControllerIntegrationTests extends BaseUnitTest {

    @Autowired
    private RestTestClient restTestClient;

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

    private <T> EntityExchangeResult<T> exchange(HttpMethod method, String uri, Object body, ParameterizedTypeReference<T> type) {
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
    void testCreateCategory() {

        ApiCategoryRequest request = new ApiCategoryRequest();
        request.setName("Book Category");
        request.setDescription("Book Category Description");

        EntityExchangeResult<ApiCategoryResponse> response = exchange(HttpMethod.POST, "/categories/", request, ApiCategoryResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getResponseBody());
        assertNotNull(response.getResponseBody().getId());
        assertEquals(request.getName(), response.getResponseBody().getName());
        assertEquals(request.getDescription(), response.getResponseBody().getDescription());
    }

    @Test
    @Order(value = 2)
    void testGetAllCategories() {

        EntityExchangeResult<String> response = exchange(HttpMethod.GET, "/categories/", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    @Order(value = 3)
    void testGetCategoryByName() {
        EntityExchangeResult<ApiCategoryResponse> response = exchange(HttpMethod.GET, "/categories/Electronics Category", null, ApiCategoryResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals("Electronics Category", response.getResponseBody().getName());
    }

    @Test
    @Order(value = 4)
    void testGetCategoryByName_ShouldFail_WhenCategoryIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.GET, "/categories/Book Categoryy", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 6)
    void testCreateCategory_ShouldFail_WhenCategoryCodeIsAlreadyExists() {
        ApiCategoryRequest apiCategoryRequest = getApiCategoryRequest();

        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.POST, "/categories/", apiCategoryRequest, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.ALREADY_EXISTS.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 7)
    void testDeleteCategory() {
        EntityExchangeResult<String> response = exchange(HttpMethod.DELETE, "/categories/2", null, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("Category deleted successfully.", response.getResponseBody());
    }

    @Test
    @Order(value = 8)
    void testDeleteCategory_ShouldFail_WhenCategoryIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/categories/15", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 9)
    void testAddProductToCategory() {
        // Use "Novel" instead of "Art" because "Art" (id=6) may be soft-deleted by ProductControllerIntegrationTests
        EntityExchangeResult<ApiCategoryResponse> response = exchange(HttpMethod.PUT, "/categories/Book Category/Novel", null, ApiCategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 10)
    void testAddProductToCategory_ShouldFail_WhenProductIsNotFound() {
        // Use "Electronics Category" instead of "Beauty Category" because "Beauty Category" is deleted in Order 7
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.PUT, "/categories/Electronics Category/Perfumes", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 11)
    void testGetProductsByCategory() {
        // Use "Electronics Category" instead of "Beauty Category" because "Beauty Category" is deleted in Order 7
        EntityExchangeResult<List<ApiProductResponse>> response = exchange(HttpMethod.GET, "/categories/Electronics Category/products", null, new ParameterizedTypeReference<>() {
        });

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 12)
    void testGetProductsByCategoryName_ShouldFail_WhenCategoryIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.GET, "/categories/Automotive Category/products", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 13)
    void testDeleteProductFromCategory() {
        // Use "Novel" to match what was added in Order 9
        EntityExchangeResult<ApiCategoryResponse> response = exchange(HttpMethod.DELETE, "/categories/Book Category/Novel", null, ApiCategoryResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getResponseBody());
    }

    @Test
    @Order(value = 14)
    void testDeleteProductFromCategory_ShouldFail_WhenCategoryIsNotFound() {
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/categories/Automotive Category/BMW", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }

    @Test
    @Order(value = 15)
    void testDeleteProductFromCategory_ShouldFail_WhenProductIsNotFound() {
        // Use "Electronics Category" instead of "Beauty Category" because "Beauty Category" is deleted in Order 7
        EntityExchangeResult<ErrorResponse> response = exchange(HttpMethod.DELETE, "/categories/Electronics Category/Perfumes", null, ErrorResponse.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNotNull(response.getResponseBody());
        Assertions.assertEquals(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getResponseBody().getErrorCode());
    }
}
