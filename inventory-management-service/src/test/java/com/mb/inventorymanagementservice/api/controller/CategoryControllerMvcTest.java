package com.mb.inventorymanagementservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.response.ApiCategoryResponse;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.config.MvcTestSecurityConfig;
import com.mb.inventorymanagementservice.data.entity.Category;
import com.mb.inventorymanagementservice.mapper.CategoryMapper;
import com.mb.inventorymanagementservice.mapper.ProductMapper;
import com.mb.inventorymanagementservice.service.CategoryService;
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
 * MVC tests for {@link CategoryController}.
 * <p>
 * Uses {@link MvcTestSecurityConfig} which enables {@code @PreAuthorize} method security
 * and removes the {@code ROLE_} prefix. The production {@code AuthTokenFilter} is excluded
 * because {@code @WithMockUser} sets the SecurityContext directly without needing a JWT.
 */
@Import(MvcTestSecurityConfig.class)
@WebMvcTest(CategoryController.class)
class CategoryControllerMvcTest extends BaseUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Nested
    @DisplayName("GET /categories/")
    class GetAllCategories {

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void getAllCategories_ShouldReturnPageOfCategories_WhenAuthorized() throws Exception {
            // Arrange
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(category.getName())
                    .description(category.getDescription())
                    .liveInMarket(category.isLiveInMarket())
                    .build();
            Page<Category> categoryPage = new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1);
            Page<ApiCategoryResponse> responsePage = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

            when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoryPage);
            when(categoryMapper.map(categoryPage)).thenReturn(responsePage);

            // Act
            // Assertions
            mockMvc.perform(get("/categories/")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value(category.getName()))
                    .andExpect(jsonPath("$.content[0].description").value(category.getDescription()));

            verify(categoryService).getAllCategories(any(Pageable.class));
        }

        @Test
        void getAllCategories_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(get("/categories/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "OTHER_ROLE")
        void getAllCategories_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(get("/categories/")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /categories/{name}")
    class GetCategoryByName {

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void getCategoryByName_ShouldReturnCategory_WhenAuthorized() throws Exception {
            // Arrange
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(category.getName())
                    .description(category.getDescription())
                    .liveInMarket(category.isLiveInMarket())
                    .build();

            when(categoryService.findByName("Beauty Category")).thenReturn(category);
            when(categoryMapper.map(category)).thenReturn(response);

            // Act
            // Assertions
            mockMvc.perform(get("/categories/{name}", "Beauty Category")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Beauty Category"));

            verify(categoryService).findByName("Beauty Category");
        }

        @Test
        void getCategoryByName_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(get("/categories/{name}", "Beauty Category")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /categories/")
    class CreateCategory {

        @Test
        @WithMockUser(authorities = "CREATE_CATEGORY")
        void createCategory_ShouldReturnCreatedCategory_WhenAuthorized() throws Exception {
            // Arrange
            ApiCategoryRequest request = getApiCategoryRequest();
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            when(categoryMapper.map(any(ApiCategoryRequest.class))).thenReturn(category);
            when(categoryService.createCategory(any(Category.class))).thenReturn(category);
            when(categoryMapper.map(category)).thenReturn(response);

            String requestJson = """
                    {
                      "name": "Electronics Category",
                      "description": "Electronics Category Description"
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(post("/categories/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(request.getName()))
                    .andExpect(jsonPath("$.description").value(request.getDescription()));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void createCategory_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(post("/categories/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void createCategory_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(post("/categories/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /categories/{categoryId}")
    class UpdateCategoryById {

        @Test
        @WithMockUser(authorities = "UPDATE_CATEGORY")
        void updateCategoryById_ShouldReturnUpdatedCategory_WhenAuthorized() throws Exception {
            // Arrange
            ApiCategoryRequest request = getApiCategoryRequest();
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            when(categoryMapper.map(any(ApiCategoryRequest.class))).thenReturn(category);
            when(categoryService.updateCategoryById(eq(1L), any(Category.class))).thenReturn(category);
            when(categoryMapper.map(category)).thenReturn(response);

            String requestJson = """
                    {
                      "name": "Electronics Category",
                      "description": "Electronics Category Description"
                    }
                    """;

            // Act
            // Assertions
            mockMvc.perform(put("/categories/{categoryId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(request.getName()));

            verify(categoryService).updateCategoryById(eq(1L), any(Category.class));
        }

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void updateCategoryById_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(put("/categories/{categoryId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /categories/{categoryId}")
    class DeleteCategoryById {

        @Test
        @WithMockUser(authorities = "DELETE_CATEGORY")
        void deleteCategoryById_ShouldReturnOk_WhenAuthorized() throws Exception {
            // Arrange
            doNothing().when(categoryService).deleteCategoryById(1L);

            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{categoryId}", 1L))
                    .andExpect(status().isOk());

            verify(categoryService).deleteCategoryById(1L);
        }

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void deleteCategoryById_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{categoryId}", 1L))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteCategoryById_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{categoryId}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /categories/{name}/products")
    class GetProductsByCategory {

        @Test
        @WithMockUser(authorities = "GET_PRODUCT")
        void getProductsByCategory_ShouldReturnProducts_WhenAuthorized() throws Exception {
            // Arrange
            Category category = getCategory();
            ApiProductResponse productResponse = ApiProductResponse.builder()
                    .id(1L)
                    .name("IPHONE 13")
                    .productCode("IPHONE_13")
                    .build();

            when(categoryService.findByName("Beauty Category")).thenReturn(category);
            when(productMapper.map(category.getProducts())).thenReturn(List.of(productResponse));

            // Act
            // Assertions
            mockMvc.perform(get("/categories/{name}/products", "Beauty Category")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("IPHONE 13"));

            verify(categoryService).findByName("Beauty Category");
        }

        @Test
        void getProductsByCategory_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(get("/categories/{name}/products", "Beauty Category")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /categories/{name}/{productName}")
    class AddProductToCategory {

        @Test
        @WithMockUser(authorities = "ADD_PRODUCT")
        void addProductToCategory_ShouldReturnCategory_WhenAuthorized() throws Exception {
            // Arrange
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();

            when(categoryService.addProductToCategory("Beauty Category", "IPHONE 13")).thenReturn(category);
            when(categoryMapper.map(category)).thenReturn(response);

            // Act
            // Assertions
            mockMvc.perform(put("/categories/{name}/{productName}", "Beauty Category", "IPHONE 13"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Beauty Category"));

            verify(categoryService).addProductToCategory("Beauty Category", "IPHONE 13");
        }

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void addProductToCategory_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(put("/categories/{name}/{productName}", "Beauty Category", "IPHONE 13"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /categories/{name}/{productName}")
    class DeleteProductFromCategory {

        @Test
        @WithMockUser(authorities = "REMOVE_PRODUCT")
        void deleteProductFromCategory_ShouldReturnCategory_WhenAuthorized() throws Exception {
            // Arrange
            Category category = getCategory();
            ApiCategoryResponse response = ApiCategoryResponse.builder()
                    .id(1L)
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();

            when(categoryService.deleteProductFromCategory("Beauty Category", "IPHONE 13")).thenReturn(category);
            when(categoryMapper.map(category)).thenReturn(response);

            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{name}/{productName}", "Beauty Category", "IPHONE 13"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Beauty Category"));

            verify(categoryService).deleteProductFromCategory("Beauty Category", "IPHONE 13");
        }

        @Test
        @WithMockUser(authorities = "GET_CATEGORY")
        void deleteProductFromCategory_ShouldReturnForbidden_WhenWrongAuthority() throws Exception {
            // Arrange — user with wrong authority
            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{name}/{productName}", "Beauty Category", "IPHONE 13"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteProductFromCategory_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
            // Arrange — no authentication
            // Act
            // Assertions
            mockMvc.perform(delete("/categories/{name}/{productName}", "Beauty Category", "IPHONE 13"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
