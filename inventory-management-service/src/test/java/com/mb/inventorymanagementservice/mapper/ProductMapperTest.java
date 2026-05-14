package com.mb.inventorymanagementservice.mapper;

import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.base.BaseUnitTest;
import com.mb.inventorymanagementservice.data.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMapperTest extends BaseUnitTest {

    ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void map_ProductToApiProductResponse_ShouldSucceed() {
        // Arrange
        Product product = getProduct();

        // Act
        ApiProductResponse result = productMapper.map(product);

        // Assertions
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
    }

    @Test
    void map_ListOfProductToListOfApiProductResponse_ShouldSucceed() {
        // Arrange
        List<Product> apiProductRequest = getProducts();

        // Act
        List<ApiProductResponse> result = productMapper.map(apiProductRequest);

        // Assertions
        assertEquals(apiProductRequest.getFirst().getName(), result.getFirst().getName());
        assertEquals(apiProductRequest.getFirst().getDescription(), result.getFirst().getDescription());
        assertEquals(apiProductRequest.getFirst().getPrice(), result.getFirst().getPrice());
        assertEquals(apiProductRequest.getFirst().getId(), result.getFirst().getId());
    }

    @Test
    void map_ApiProductRequestToProduct_ShouldSucceed() {
        // Arrange
        ApiProductRequest apiProductRequest = getApiProductRequest();

        // Act
        Product result = productMapper.map(apiProductRequest);

        // Assertions
        assertEquals(apiProductRequest.getName(), result.getName());
        assertEquals(apiProductRequest.getDescription(), result.getDescription());
        assertEquals(apiProductRequest.getPrice(), result.getPrice());
    }
}
