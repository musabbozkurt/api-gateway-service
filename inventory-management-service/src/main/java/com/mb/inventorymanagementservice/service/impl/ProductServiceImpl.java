package com.mb.inventorymanagementservice.service.impl;

import com.mb.inventorymanagementservice.data.entity.Product;
import com.mb.inventorymanagementservice.data.repository.ProductRepository;
import com.mb.inventorymanagementservice.exception.BaseException;
import com.mb.inventorymanagementservice.exception.InventoryManagementServiceErrorCode;
import com.mb.inventorymanagementservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Product createProduct(Product product) {
        productRepository.findByNameIgnoreCase(product.getName()).ifPresent(_ -> {
            throw new BaseException(InventoryManagementServiceErrorCode.ALREADY_EXISTS);
        });
        return productRepository.save(product);
    }

    @Override
    public Product updateProductById(Long productId, Product newProduct) {
        Product existingProduct = getProductById(productId);
        boolean shouldDatabaseBeUpdated = false;
        if (!Strings.CI.equals(existingProduct.getName(), newProduct.getName())) {
            existingProduct.setName(newProduct.getName());
            shouldDatabaseBeUpdated = true;
        }
        if (!Strings.CI.equals(existingProduct.getProductCode(), newProduct.getProductCode())) {
            existingProduct.setProductCode(newProduct.getProductCode());
            shouldDatabaseBeUpdated = true;
        }
        if (!Strings.CI.equals(existingProduct.getDescription(), newProduct.getDescription())) {
            existingProduct.setDescription(newProduct.getDescription());
            shouldDatabaseBeUpdated = true;
        }
        if (ObjectUtils.notEqual(existingProduct.getPrice(), newProduct.getPrice())) {
            existingProduct.setPrice(newProduct.getPrice());
            shouldDatabaseBeUpdated = true;
        }
        if (ObjectUtils.notEqual(existingProduct.getQuantity(), newProduct.getQuantity())) {
            existingProduct.setQuantity(newProduct.getQuantity());
            shouldDatabaseBeUpdated = true;
        }
        if (shouldDatabaseBeUpdated) {
            return productRepository.save(existingProduct);
        }
        return existingProduct;
    }

    @Override
    public void deleteProductById(Long productId) {
        productRepository.deleteById(getProductById(productId).getId());
    }

    @Override
    public Product getProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new BaseException(InventoryManagementServiceErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Integer getQuantityByProductCode(String productCode) {
        return productRepository.findQuantityByProductCode(productCode);
    }
}
