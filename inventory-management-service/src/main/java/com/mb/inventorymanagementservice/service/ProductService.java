package com.mb.inventorymanagementservice.service;

import com.mb.inventorymanagementservice.data.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<Product> getAllProducts(Pageable pageable);

    Product getProductById(Long productId);

    Product createProduct(Product product);

    Product updateProductById(Long productId, Product product);

    void deleteProductById(Long productId);

    Product getProductByName(String name);

    Integer getQuantityByProductCode(String productCode);
}
