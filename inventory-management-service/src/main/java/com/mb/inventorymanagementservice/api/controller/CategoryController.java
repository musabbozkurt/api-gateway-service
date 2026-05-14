package com.mb.inventorymanagementservice.api.controller;

import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.response.ApiCategoryResponse;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.mapper.CategoryMapper;
import com.mb.inventorymanagementservice.mapper.ProductMapper;
import com.mb.inventorymanagementservice.service.CategoryService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @GetMapping("/")
    @Observed(name = "getAllCategories")
    @PreAuthorize("hasRole('GET_CATEGORY')")
    @Operation(description = "Get all categories.")
    public ResponseEntity<Page<ApiCategoryResponse>> getAllCategories(Pageable pageable) {
        log.info("Received a request to get all categories. getAllCategories.");
        return new ResponseEntity<>(categoryMapper.map(categoryService.getAllCategories(pageable)), HttpStatus.OK);
    }

    @GetMapping("/{name}")
    @Observed(name = "getCategoryByName")
    @PreAuthorize("hasRole('GET_CATEGORY')")
    @Operation(description = "Get category by name.")
    public ResponseEntity<ApiCategoryResponse> getCategoryByName(@PathVariable String name) {
        log.info("Received a request to get category by name. getCategoryByName - name: {}", name);
        return new ResponseEntity<>(categoryMapper.map(categoryService.findByName(name)), HttpStatus.OK);
    }

    @PostMapping("/")
    @Observed(name = "createCategory")
    @Operation(description = "Create category.")
    @PreAuthorize("hasRole('CREATE_CATEGORY')")
    public ResponseEntity<ApiCategoryResponse> createCategory(@RequestBody ApiCategoryRequest apiCategoryRequest) {
        log.info("Received a request to create category. createCategory - apiCategoryRequest: {}", apiCategoryRequest);
        return new ResponseEntity<>(categoryMapper.map(categoryService.createCategory(categoryMapper.map(apiCategoryRequest))), HttpStatus.CREATED);
    }

    @PutMapping("/{categoryId}")
    @Observed(name = "updateCategoryById")
    @PreAuthorize("hasRole('UPDATE_CATEGORY')")
    @Operation(description = "Update category by id.")
    public ResponseEntity<ApiCategoryResponse> updateCategoryById(@PathVariable Long categoryId, @RequestBody ApiCategoryRequest apiCategoryRequest) {
        log.info("Received a request to update category by id. updateCategoryById - categoryId: {}, apiCategoryRequest: {}", categoryId, apiCategoryRequest);
        return new ResponseEntity<>(categoryMapper.map(categoryService.updateCategoryById(categoryId, categoryMapper.map(apiCategoryRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/{categoryId}")
    @Observed(name = "deleteCategoryById")
    @PreAuthorize("hasRole('DELETE_CATEGORY')")
    @Operation(description = "Delete category by id")
    public ResponseEntity<String> deleteCategoryById(@PathVariable Long categoryId) {
        log.info("Received a request to delete category by id. deleteCategoryById - categoryId: {}", categoryId);
        categoryService.deleteCategoryById(categoryId);
        return new ResponseEntity<>("Category deleted successfully.", HttpStatus.OK);
    }

    @GetMapping("/{name}/products")
    @PreAuthorize("hasRole('GET_PRODUCT')")
    @Observed(name = "getProductsByCategory")
    @Operation(description = "List products of a specific category.")
    public ResponseEntity<List<ApiProductResponse>> getProductsByCategory(@PathVariable String name) {
        log.info("Received a request to get products by category name. getProductsByCategory - name: {}", name);
        return ResponseEntity.ok(productMapper.map(categoryService.findByName(name).getProducts()));
    }

    @PutMapping("/{name}/{productName}")
    @PreAuthorize("hasRole('ADD_PRODUCT')")
    @Observed(name = "addProductToCategory")
    @Operation(description = "Add a product to a category.")
    public ResponseEntity<ApiCategoryResponse> addProductToCategory(@PathVariable String name, @PathVariable String productName) {
        log.info("Received a request to add product to category. addProductToCategory - name: {}, productName: {}", name, productName);
        return ResponseEntity.ok(categoryMapper.map(categoryService.addProductToCategory(name, productName)));
    }

    @DeleteMapping("/{name}/{productName}")
    @PreAuthorize("hasRole('REMOVE_PRODUCT')")
    @Observed(name = "deleteProductFromCategory")
    @Operation(description = "Delete a product from a category.")
    public ResponseEntity<ApiCategoryResponse> deleteProductFromCategory(@PathVariable String name, @PathVariable String productName) {
        log.info("Received a request to delete product from category. deleteProductFromCategory - name: {}, productName: {}", name, productName);
        return ResponseEntity.ok(categoryMapper.map(categoryService.deleteProductFromCategory(name, productName)));
    }
}
