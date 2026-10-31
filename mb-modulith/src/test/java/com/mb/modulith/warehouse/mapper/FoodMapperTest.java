package com.mb.modulith.warehouse.mapper;

import com.mb.modulith.warehouse.api.request.ApiFoodRequest;
import com.mb.modulith.warehouse.api.request.ApiUpdateFoodRequest;
import com.mb.modulith.warehouse.api.response.ApiFoodResponse;
import com.mb.modulith.warehouse.data.model.Food;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FoodMapperTest {

    @Test
    void map_ShouldReturnNull_WhenCreateRequestIsNull() {
        // Act
        Food result = FoodMapper.map((ApiFoodRequest) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldMapCreateRequest_WhenRequestIsValid() {
        // Arrange
        ApiFoodRequest request = new ApiFoodRequest();
        request.setName("Apple");
        request.setWeight(BigDecimal.TEN);

        // Act
        Food result = FoodMapper.map(request);

        // Assertions
        assertEquals("Apple", result.getName());
        assertEquals(BigDecimal.TEN, result.getWeight());
    }

    @Test
    void map_ShouldUpdateFood_WhenUpdateRequestIsValid() {
        // Arrange
        Food existing = new Food(1, "Old", BigDecimal.ONE);
        ApiUpdateFoodRequest request = new ApiUpdateFoodRequest();
        request.setName("Banana");
        request.setWeight(BigDecimal.valueOf(3));

        // Act
        Food result = FoodMapper.map(existing, request);

        // Assertions
        assertEquals("Banana", result.getName());
        assertEquals(BigDecimal.valueOf(3), result.getWeight());
    }

    @Test
    void map_ShouldReturnNull_WhenFoodIsNull() {
        // Act
        ApiFoodResponse result = FoodMapper.map((Food) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldMapFoodToResponse_WhenFoodIsValid() {
        // Arrange
        Food food = new Food(5, "Milk", BigDecimal.valueOf(2));
        food.setCreatedDateTime(OffsetDateTime.now());
        food.setModifiedDateTime(OffsetDateTime.now());

        // Act
        ApiFoodResponse result = FoodMapper.map(food);

        // Assertions
        assertEquals(5, result.getId());
        assertEquals("Milk", result.getName());
        assertEquals(BigDecimal.valueOf(2), result.getWeight());
    }

    @Test
    void map_ShouldMapFoodList_WhenListIsNotEmpty() {
        // Arrange
        Food food = new Food(1, "Bread", BigDecimal.ONE);

        // Act
        List<ApiFoodResponse> result = FoodMapper.map(List.of(food));

        // Assertions
        assertEquals(1, result.size());
        assertEquals("Bread", result.getFirst().getName());
    }
}
