package com.mb.modulith.warehouse.mapper;

import com.mb.modulith.warehouse.api.request.ApiFoodRequest;
import com.mb.modulith.warehouse.api.request.ApiUpdateFoodRequest;
import com.mb.modulith.warehouse.api.response.ApiFoodResponse;
import com.mb.modulith.warehouse.data.model.Food;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FoodMapper {

    public static Food map(ApiFoodRequest apiFoodRequest) {
        if (apiFoodRequest == null) {
            return null;
        }
        Food food = new Food();
        food.setName(apiFoodRequest.getName());
        food.setWeight(apiFoodRequest.getWeight());
        return food;
    }

    public static Food map(Food oldFood, ApiUpdateFoodRequest apiUpdateFoodRequest) {
        oldFood.setName(apiUpdateFoodRequest.getName());
        oldFood.setWeight(apiUpdateFoodRequest.getWeight());
        return oldFood;
    }

    public static ApiFoodResponse map(Food food) {
        if (food == null) {
            return null;
        }
        ApiFoodResponse apiFoodResponse = new ApiFoodResponse();
        apiFoodResponse.setId(food.getId());
        apiFoodResponse.setCreatedDateTime(food.getCreatedDateTime());
        apiFoodResponse.setModifiedDateTime(food.getModifiedDateTime());
        apiFoodResponse.setDeleted(food.isDeleted());
        apiFoodResponse.setName(food.getName());
        apiFoodResponse.setWeight(food.getWeight());
        return apiFoodResponse;
    }

    public static List<ApiFoodResponse> map(List<Food> foods) {
        return foods.stream().map(FoodMapper::map).toList();
    }
}
