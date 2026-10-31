package com.mb.modulith.warehouse.service.impl;

import com.mb.modulith.warehouse.data.model.Food;
import com.mb.modulith.warehouse.data.repository.FoodRepository;
import com.mb.modulith.warehouse.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;

    @Override
    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    @Override
    public Food findById(int id) {
        return foodRepository.findById(id).orElseThrow();
    }

    @Override
    public List<Food> findAllFoods() {
        return foodRepository.findAll();
    }

    @Override
    public Food updateFood(Food food) {
        return foodRepository.save(food);
    }

    @Override
    public void deleteFood(int id) {
        Food food = findById(id);
        food.setDeleted(true);
        foodRepository.save(food);
    }

    @Override
    public BigDecimal getWeightOfAllFoods() {
        return findAllFoods().stream().map(Food::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getWeightOfFoodByName(String name) {
        return foodRepository.findByName(name).stream().map(Food::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
