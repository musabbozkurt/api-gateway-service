package com.mb.modulith.warehouse.api.controller;

import com.mb.modulith.support.JacksonTestConfiguration;
import com.mb.modulith.warehouse.api.request.ApiFoodRequest;
import com.mb.modulith.warehouse.data.model.Food;
import com.mb.modulith.warehouse.service.FoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = FoodController.class)
@ContextConfiguration(classes = FoodController.class)
class FoodControllerTest {

    @MockitoBean
    private FoodService foodService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFood_ShouldReturnMappedResponse_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiFoodRequest request = new ApiFoodRequest("Orange", BigDecimal.valueOf(23.32));
        Food saved = new Food(1, "Orange", BigDecimal.valueOf(23.32));
        when(foodService.createFood(any(Food.class))).thenReturn(saved);

        // Act
        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Orange"))
                .andExpect(jsonPath("$.weight").value(23.32));
    }
}
