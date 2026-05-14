package com.mb.inventorymanagementservice.base;

import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.data.entity.Category;
import com.mb.inventorymanagementservice.data.entity.Product;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.List;

public abstract class BaseUnitTest {

    public static Product getProduct() {
        Product product = new Product();
        product.setName("IPHONE 13");
        product.setProductCode("IPHONE_13");
        product.setDescription("IPHONE 13 Description");
        product.setPrice(Money.of(BigDecimal.valueOf(22.12), "EUR"));
        product.setQuantity(10);
        return product;
    }

    public static List<Product> getProducts() {
        return List.of(getProduct());
    }

    public static ApiProductRequest getApiProductRequest() {
        return ApiProductRequest.builder().name("IPHONE 15").productCode("IPHONE_15").description("IPHONE 15 Description").price(Money.of(BigDecimal.valueOf(2850), "EUR")).quantity(10).build();
    }

    public static ApiProductRequest getApiProductRequest2() {
        return ApiProductRequest.builder().name("IPHONE 13").productCode("IPHONE_13").description("IPHONE 13 Description").price(Money.of(BigDecimal.valueOf(2500), "EUR")).quantity(10).build();
    }

    public static ApiCategoryRequest getApiCategoryRequest() {
        ApiCategoryRequest apiCategoryRequest = new ApiCategoryRequest();
        apiCategoryRequest.setName("Electronics Category");
        apiCategoryRequest.setDescription("Electronics Category Description");
        return apiCategoryRequest;
    }

    public static Category getCategory() {
        Category category = new Category();
        category.setName("Beauty Category");
        category.setDescription("Beauty Category Description");
        category.setLiveInMarket(false);
        category.setProducts(List.of(getProduct()));
        return category;
    }
}
