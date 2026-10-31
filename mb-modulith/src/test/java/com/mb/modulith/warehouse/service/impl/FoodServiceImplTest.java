package com.mb.modulith.warehouse.service.impl;

import com.mb.modulith.warehouse.data.model.Food;
import com.mb.modulith.warehouse.data.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodServiceImpl foodService;

    @Test
    void createFood_ShouldPersist_WhenFoodIsValid() {
        // Arrange
        Food food = new Food(0, "Apple", BigDecimal.TEN);
        when(foodRepository.save(any(Food.class))).thenReturn(food);

        // Act
        Food result = foodService.createFood(food);

        // Assertions
        assertEquals("Apple", result.getName());
    }

    @Test
    void findById_ShouldReturnFood_WhenExists() {
        // Arrange
        Food food = new Food(1, "Apple", BigDecimal.TEN);
        when(foodRepository.findById(1)).thenReturn(Optional.of(food));

        // Act
        Food result = foodService.findById(1);

        // Assertions
        assertEquals(1, result.getId());
    }

    @Test
    void findAllFoods_ShouldReturnList_WhenRepositoryHasRows() {
        // Arrange
        when(foodRepository.findAll()).thenReturn(List.of(new Food(1, "A", BigDecimal.ONE)));

        // Act
        List<Food> result = foodService.findAllFoods();

        // Assertions
        assertEquals(1, result.size());
    }
}