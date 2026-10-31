package com.mb.modulith.delivery.api.controller;

import com.mb.modulith.delivery.api.request.ApiDeliveryRequest;
import com.mb.modulith.delivery.api.response.ApiTopCourierResponse;
import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.mapper.DeliveryMapper;
import com.mb.modulith.delivery.service.DeliveryService;
import com.mb.modulith.support.CommerceWebMvcTestSecurity;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = DeliveryController.class)
@ContextConfiguration(classes = DeliveryController.class)
class DeliveryControllerTest extends BaseUnitTest {

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private DeliveryMapper deliveryMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createNewDelivery_ShouldReturnDelivery_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiDeliveryRequest request = getApiDeliveryRequest();
        Delivery entity = getDelivery();
        when(deliveryMapper.map(any(ApiDeliveryRequest.class))).thenReturn(entity);
        when(deliveryService.save(entity)).thenReturn(entity);

        // Act
        ResultActions result = mockMvc.perform(post("/api/delivery")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value(0));
    }

    @Test
    void getDeliveryById_ShouldReturnDelivery_WhenDeliveryExists() throws Exception {
        // Arrange
        Delivery delivery = getDelivery();
        delivery.setId(5L);
        when(deliveryService.findById(5L)).thenReturn(delivery);

        // Act
        ResultActions result = mockMvc.perform(get("/api/delivery/{delivery-id}", 5L));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getDeliveryById_ShouldReturn404_WhenDeliveryMissing() throws Exception {
        // Arrange
        when(deliveryService.findById(99L)).thenReturn(null);

        // Act
        ResultActions result = mockMvc.perform(get("/api/delivery/{delivery-id}", 99L));

        // Assertions
        result.andExpect(status().isNotFound());
    }

    @Test
    void getTopCouriers_ShouldReturnRankedList_WhenIntervalIsValid() throws Exception {
        // Arrange
        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2020-01-02T00:00:00Z");
        List<ApiTopCourierResponse> topCouriers = List.of(
                ApiTopCourierResponse.builder()
                        .deliveryManId(2L)
                        .deliveryManName("name")
                        .maxOrderPrice(200L)
                        .averageCommission(15d)
                        .build());
        when(deliveryService.findTopCouriersByMaxOrderPrice(from, to, 3)).thenReturn(topCouriers);

        // Act
        ResultActions result = mockMvc.perform(get("/api/delivery/top-couriers")
                .param("from", from.toString())
                .param("to", to.toString())
                .param("limit", "3"));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maxOrderPrice").value(200))
                .andExpect(jsonPath("$[0].deliveryManId").value(2));
    }
}
