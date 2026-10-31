package com.mb.modulith.warehouse.api.controller;

import com.mb.modulith.warehouse.api.request.ApiFoodRequest;
import com.mb.modulith.warehouse.api.request.ApiUpdateFoodRequest;
import com.mb.modulith.warehouse.api.response.ApiFoodResponse;
import com.mb.modulith.warehouse.mapper.FoodMapper;
import com.mb.modulith.warehouse.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/foods")
@Tag(name = "Warehouse - Foods", description = "Food inventory API")
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a food item")
    public ApiFoodResponse createFood(@RequestBody ApiFoodRequest apiFoodRequest) {
        log.info("Received request to create food. createFood - apiFoodRequest: {}", apiFoodRequest);
        return FoodMapper.map(foodService.createFood(FoodMapper.map(apiFoodRequest)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get food by id")
    public ApiFoodResponse getFoodById(@PathVariable int id) {
        log.info("Received request to get food by id. getFoodById - id: {}", id);
        return FoodMapper.map(foodService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "List all foods")
    public List<ApiFoodResponse> findAllFoods() {
        log.info("Received request to find all foods. findAllFoods.");
        return FoodMapper.map(foodService.findAllFoods());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update food by id")
    public ApiFoodResponse updateFood(@PathVariable int id, @RequestBody ApiUpdateFoodRequest apiUpdateFoodRequest) {
        log.info("Received request to update food. updateFood - id: {}, apiUpdateFoodRequest: {}", id, apiUpdateFoodRequest);
        return FoodMapper.map(foodService.updateFood(FoodMapper.map(foodService.findById(id), apiUpdateFoodRequest)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Soft-delete food by id")
    public void deleteFood(@PathVariable int id) {
        log.info("Received request to delete food. deleteFood - id: {}", id);
        foodService.deleteFood(id);
    }

    @GetMapping("/total-weight")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Sum weight of all foods")
    public BigDecimal getWeightOfAllFoods() {
        log.info("Received request to get weight of all foods. getWeightOfAllFoods.");
        return foodService.getWeightOfAllFoods();
    }

    @GetMapping("/total-weight/{name}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Sum weight of foods matching name")
    public BigDecimal getWeightOfFoodByName(@PathVariable String name) {
        log.info("Received request to get weight of food by name. getWeightOfFoodByName - name: {}", name);
        return foodService.getWeightOfFoodByName(name);
    }
}
