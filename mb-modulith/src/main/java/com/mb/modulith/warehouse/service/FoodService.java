package com.mb.modulith.warehouse.service;

import com.mb.modulith.warehouse.data.model.Food;

import java.math.BigDecimal;
import java.util.List;

public interface FoodService {

    Food createFood(Food food);

    Food findById(int id);

    List<Food> findAllFoods();

    Food updateFood(Food food);

    void deleteFood(int id);

    BigDecimal getWeightOfAllFoods();

    BigDecimal getWeightOfFoodByName(String name);
}
