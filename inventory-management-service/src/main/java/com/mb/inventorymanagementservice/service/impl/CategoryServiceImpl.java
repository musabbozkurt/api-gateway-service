package com.mb.inventorymanagementservice.service.impl;

import com.mb.inventorymanagementservice.data.entity.Category;
import com.mb.inventorymanagementservice.data.repository.CategoryRepository;
import com.mb.inventorymanagementservice.exception.BaseException;
import com.mb.inventorymanagementservice.exception.InventoryManagementServiceErrorCode;
import com.mb.inventorymanagementservice.service.CategoryService;
import com.mb.inventorymanagementservice.service.ProductService;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name).orElseThrow(() -> new BaseException(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new BaseException(InventoryManagementServiceErrorCode.ALREADY_EXISTS);
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategoryById(Long categoryId, Category newCategory) {
        categoryRepository.findById(categoryId)
                .ifPresentOrElse(_ -> newCategory.setId(categoryId),
                        () -> {
                            throw new BaseException(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND);
                        });
        return categoryRepository.save(newCategory);
    }

    @Override
    public void deleteCategoryById(Long categoryId) {
        categoryRepository
                .deleteById(categoryRepository
                        .findById(categoryId)
                        .filter(category -> category.getProducts().isEmpty())
                        .orElseThrow(() -> new BaseException(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND))
                        .getId());
    }

    @Override
    @Transactional
    public Category addProductToCategory(String name, String productName) {
        Category category = this.findByName(name);
        category.addProduct(productService.getProductByName(productName));
        category.setLiveInMarket(isLiveInMarket(category));
        try {
            return categoryRepository.save(category);
        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic locking failure occurred while saving category. addProductToCategory - Exception: {}", e.getMessage());
            throw new BaseException(InventoryManagementServiceErrorCode.OPTIMISTIC_LOCKING_FAILURE);
        }
    }

    @Override
    @Transactional
    public Category deleteProductFromCategory(String name, String productName) {
        try {
            Category category = categoryRepository.findCategoryByNameIgnoreCase(name).orElseThrow(() -> new BaseException(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND));
            category.removeProduct(productService.getProductByName(productName));
            category.setLiveInMarket(isLiveInMarket(category));
            return categoryRepository.save(category);
        } catch (PessimisticLockException e) {
            log.error("Pessimistic lock exception occurred while deleting product from category. deleteProductFromCategory - Exception: {}", e.getMessage());
            throw new BaseException(InventoryManagementServiceErrorCode.PESSIMISTIC_LOCKING_FAILURE);
        }
    }

    private boolean isLiveInMarket(Category category) {
        return !category.getProducts().isEmpty();
    }
}
