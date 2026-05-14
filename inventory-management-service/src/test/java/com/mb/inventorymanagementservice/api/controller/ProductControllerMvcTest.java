package com.mb.inventorymanagementservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.config.MvcTestSecurityConfig;
import com.mb.inventorymanagementservice.data.entity.Product;
import com.mb.inventorymanagementservice.mapper.ProductMapper;
import com.mb.inventorymanagementservice.service.ProductService;
import com.mb.inventorymanagementservice.utils.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC tests for {@link ProductController}.
 * <p>
 * Uses {@link MvcTestSecurityConfig} which enables {@code @PreAuthorize} method security
 * and removes the {@code ROLE_} prefix. The production {@code AuthTokenFilter} is excluded
 * because {@code @WithMockUser} sets the SecurityContext directly without needing a JWT.
 */
@WebMvcTest(ProductController.class)
@Import(MvcTestSecurityConfig.class)
class ProductControllerMvcTest extends BaseUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private JwtUtils jwtUtils;


    @Nested
    @DisplayName("GET /products/")
    class GetAllProducts {

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void getAllProducts_ShouldReturnPageOfProducts_WhenAuthorized() throws Exception {
            // Arrange
            Product product = getProduct();
            ApiProductResponse response = ApiProductResponse.builder()
                    .id(1L)
                    .name(product.getName())
                    .productCode(product.getProductCode())
                    .description(product.getDescription())
                    .quantity(product.getQuantity())
                    .build();
            Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            Page<ApiProductResponse> responsePage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

            when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);
            when(productMapper.map(productPage)).thenReturn(responsePage);

            // Act
            // Assertions
            mockMvc.perform(get("/products/")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value(product.getName()))
                    .andExpect(jsonPath("$.content[0].productCode").value(product.getProductCode()));

            verify(productService).getAllProducts(any(Pageable.class));
        }

        @Test
        void getAllProducts_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication

            // Act
            // Assertions
            mockMvc.perform(get("/products/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "OTHER_ROLE")
        void getAllProducts_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority

            // Act
            // Assertions
            mockMvc.perform(get("/products/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /products/{name}")
    class GetProductByName {

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void getProductByName_ShouldReturnProduct_WhenAuthorized() throws Exception {
            // Arrange
            Product product = getProduct();
            ApiProductResponse response = ApiProductResponse.builder()
                    .id(1L)
                    .name(product.getName())
                    .productCode(product.getProductCode())
                    .description(product.getDescription())
                    .quantity(product.getQuantity())
                    .build();

            when(productService.getProductByName("IPHONE 13")).thenReturn(product);
            when(productMapper.map(product)).thenReturn(response);

            // Act
            // Assertions
            mockMvc.perform(get("/products/{name}", "IPHONE 13")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("IPHONE 13"))
                    .andExpect(jsonPath("$.productCode").value("IPHONE_13"));

            verify(productService).getProductByName("IPHONE 13");
        }

        @Test
        void getProductByName_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(get("/products/{name}", "IPHONE 13")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /products/")
    class CreateProduct {

        @Test
        @WithMockUser(authorities = "CREATE_PRODUCT")
        void createProduct_ShouldReturnCreatedProduct_WhenAuthorized() throws Exception {
            // Arrange
            ApiProductRequest request = getApiProductRequest();
            Product product = getProduct();
            ApiProductResponse response = ApiProductResponse.builder()
                    .id(1L)
                    .name(request.getName())
                    .productCode(request.getProductCode())
                    .description(request.getDescription())
                    .quantity(request.getQuantity())
                    .build();

            when(productMapper.map(any(ApiProductRequest.class))).thenReturn(product);
            when(productService.createProduct(any(Product.class))).thenReturn(product);
            when(productMapper.map(product)).thenReturn(response);

            String requestJson = """
                    {
                      "name": "IPHONE 15",
                      "productCode": "IPHONE_15",
                      "description": "IPHONE 15 Description",
                      "price": {
                        "amount": 2850,
                        "currency": "EUR"
                      },
                      "quantity": 10
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(post("/products/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(request.getName()))
                    .andExpect(jsonPath("$.productCode").value(request.getProductCode()));

            verify(productService).createProduct(any(Product.class));
        }

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void createProduct_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            String requestJson = """
                    {
                      "name": "X",
                      "productCode": "X",
                      "description": "X",
                      "price": {
                        "amount": 1,
                        "currency": "EUR"
                      },
                      "quantity": 1
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(post("/products/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden());
        }

        @Test
        void createProduct_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(post("/products/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /products/{productId}")
    class UpdateProductById {

        @Test
        @WithMockUser(authorities = "UPDATE_PRODUCT")
        void updateProductById_ShouldReturnUpdatedProduct_WhenAuthorized() throws Exception {
            // Arrange
            ApiProductRequest request = getApiProductRequest();
            Product product = getProduct();
            ApiProductResponse response = ApiProductResponse.builder()
                    .id(1L)
                    .name(request.getName())
                    .productCode(request.getProductCode())
                    .description(request.getDescription())
                    .quantity(request.getQuantity())
                    .build();

            when(productMapper.map(any(ApiProductRequest.class))).thenReturn(product);
            when(productService.updateProductById(eq(1L), any(Product.class))).thenReturn(product);
            when(productMapper.map(product)).thenReturn(response);

            String requestJson = """
                    {
                      "name": "IPHONE 15",
                      "productCode": "IPHONE_15",
                      "description": "IPHONE 15 Description",
                      "price": {
                        "amount": 2850,
                        "currency": "EUR"
                      },
                      "quantity": 10
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(request.getName()));

            verify(productService).updateProductById(eq(1L), any(Product.class));
        }

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void updateProductById_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            String requestJson = """
                    {
                      "name": "X",
                      "productCode": "X",
                      "description": "X",
                      "price": {
                        "amount": 1,
                        "currency": "EUR"
                      },
                      "quantity": 1
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /products/{productId}")
    class DeleteProductById {

        @Test
        @WithMockUser(authorities = "DELETE_PRODUCT")
        void deleteProductById_ShouldReturnOk_WhenAuthorized() throws Exception {
            // Arrange
            doNothing().when(productService).deleteProductById(1L);

            // Act
            // Assertions
            mockMvc.perform(delete("/products/{productId}", 1L))
                    .andExpect(status().isOk());

            verify(productService).deleteProductById(1L);
        }

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void deleteProductById_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(delete("/products/{productId}", 1L))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteProductById_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(delete("/products/{productId}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /products/{productCode}/quantity")
    class GetQuantityByProductCode {

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void getQuantityByProductCode_ShouldReturnQuantity_WhenAuthorized() throws Exception {
            // Arrange
            when(productService.getQuantityByProductCode("IPHONE_13")).thenReturn(10);

            // Act
            // Assertions
            mockMvc.perform(get("/products/{productCode}/quantity", "IPHONE_13")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(10));

            verify(productService).getQuantityByProductCode("IPHONE_13");
        }

        @Test
        void getQuantityByProductCode_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(get("/products/{productCode}/quantity", "IPHONE_13")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
