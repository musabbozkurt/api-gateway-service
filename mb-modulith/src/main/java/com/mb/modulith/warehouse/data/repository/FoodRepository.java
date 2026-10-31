package com.mb.modulith.warehouse.data.repository;

import com.mb.modulith.warehouse.data.model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {

    List<Food> findByName(String name);
}
