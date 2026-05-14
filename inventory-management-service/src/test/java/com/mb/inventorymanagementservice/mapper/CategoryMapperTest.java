package com.mb.inventorymanagementservice.mapper;

import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.response.ApiCategoryResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.data.entity.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryMapperTest extends BaseUnitTest {

    CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void map_CategoryToApiCategoryResponse_ShouldSucceed() {
        // Arrange
        Category category = getCategory();

        // Act
        ApiCategoryResponse result = categoryMapper.map(category);

        // Assertions
        assertEquals(category.getName(), result.getName());
        assertEquals(category.getDescription(), result.getDescription());
        assertEquals(category.isLiveInMarket(), result.isLiveInMarket());
        assertEquals(category.getProducts().getFirst().getName(), result.getProducts().getFirst().getName());
        assertEquals(category.getProducts().getFirst().getDescription(), result.getProducts().getFirst().getDescription());
        assertEquals(category.getProducts().getFirst().getPrice(), result.getProducts().getFirst().getPrice());
    }

    @Test
    void map_ApiCategoryRequestToCategory_ShouldSucceed() {
        // Arrange
        ApiCategoryRequest apiCategoryRequest = getApiCategoryRequest();

        // Act
        Category result = categoryMapper.map(apiCategoryRequest);

        // Assertions
        assertEquals(apiCategoryRequest.getName(), result.getName());
        assertEquals(apiCategoryRequest.getDescription(), result.getDescription());
    }
}
