package com.mb.inventorymanagementservice.api.controller;

import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.mapper.ProductMapper;
import com.mb.inventorymanagementservice.service.ProductService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/")
    @Observed(name = "getAllProducts")
    @PreAuthorize("hasRole('GET_PRODUCT')")
    @Operation(description = "Get all products.")
    public ResponseEntity<Page<ApiProductResponse>> getAllProducts(Pageable pageable) {
        log.info("Received a request to get all products. getAllProducts.");
        return new ResponseEntity<>(productMapper.map(productService.getAllProducts(pageable)), HttpStatus.OK);
    }

    @GetMapping("/{name}")
    @Observed(name = "getProductByName")
    @PreAuthorize("hasRole('GET_PRODUCT')")
    @Operation(description = "Get product by name.")
    public ResponseEntity<ApiProductResponse> getProductByName(@PathVariable String name) {
        log.info("Received a request to get product by name. getProductByName - name: {}", name);
        return new ResponseEntity<>(productMapper.map(productService.getProductByName(name)), HttpStatus.OK);
    }

    @PostMapping("/")
    @Observed(name = "createProduct")
    @PreAuthorize("hasRole('CREATE_PRODUCT')")
    @Operation(description = "Create product.")
    public ResponseEntity<ApiProductResponse> createProduct(@RequestBody ApiProductRequest apiProductRequest) {
        log.info("Received a request to create product. createProduct - apiProductRequest: {}", apiProductRequest);
        return new ResponseEntity<>(productMapper.map(productService.createProduct(productMapper.map(apiProductRequest))), HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    @Observed(name = "updateProductById")
    @PreAuthorize("hasRole('UPDATE_PRODUCT')")
    @Operation(description = "Update product by id.")
    public ResponseEntity<ApiProductResponse> updateProductById(@PathVariable Long productId, @RequestBody ApiProductRequest apiProductRequest) {
        log.info("Received a request to update product by id. updateProductById - productId: {}, apiProductRequest: {}", productId, apiProductRequest);
        return new ResponseEntity<>(productMapper.map(productService.updateProductById(productId, productMapper.map(apiProductRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/{productId}")
    @Observed(name = "deleteProductById")
    @PreAuthorize("hasRole('DELETE_PRODUCT')")
    @Operation(description = "Delete product by id")
    public ResponseEntity<String> deleteProductById(@PathVariable Long productId) {
        log.info("Received a request to delete product by id. deleteProductById - productId: {}", productId);
        productService.deleteProductById(productId);
        return new ResponseEntity<>("Product deleted successfully.", HttpStatus.OK);
    }

    @GetMapping("/{productCode}/quantity")
    @PreAuthorize("hasRole('GET_PRODUCT')")
    @Observed(name = "getQuantityByProductCode")
    @Operation(description = "Get quantity by product code.")
    public ResponseEntity<Integer> getQuantityByProductCode(@PathVariable String productCode) {
        log.info("Received a request to get quantity by product code. getQuantityByProductCode - productCode: {}", productCode);
        return new ResponseEntity<>(productService.getQuantityByProductCode(productCode), HttpStatus.OK);
    }
}
