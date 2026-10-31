package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiOrderRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateOrderRequest;
import com.mb.modulith.commerce.api.model.response.ApiOrderResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.mapping.CustomOrderMapper;
import com.mb.modulith.commerce.service.OrderService;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = OrderController.class)
@ContextConfiguration(classes = OrderController.class)
class OrderControllerTest extends BaseUnitTest {

    @MockitoBean
    private CustomOrderMapper customOrderMapper;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        Order order = getOrder();
        ApiOrderRequest request = new ApiOrderRequest();
        request.setClientId(getClient().getId());
        request.setDeliveryAddressId(getAddress().getId());
        when(customOrderMapper.map(any(ApiOrderRequest.class))).thenReturn(order);
        when(orderService.createOrder(eq(request.getClientId()), eq(request.getDeliveryAddressId()), any(Order.class)))
                .thenReturn(order);
        when(customOrderMapper.map(order)).thenReturn(new ApiOrderResponse());

        // Act
        mockMvc.perform(post("/api/orders")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assertions
        verify(orderService).createOrder(eq(request.getClientId()), eq(request.getDeliveryAddressId()), any(Order.class));
    }

    @Test
    void getOrderById_ShouldReturn200_WhenOrderExists() throws Exception {
        // Arrange
        UUID orderId = getOrder().getId();
        Order order = getOrder();
        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(customOrderMapper.map(order)).thenReturn(new ApiOrderResponse());

        // Act
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk());

        // Assertions
        verify(orderService).getOrderById(orderId);
    }

    @Test
    void updateOrder_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        UUID orderId = getOrder().getId();
        Order order = getOrder();
        ApiUpdateOrderRequest request = new ApiUpdateOrderRequest();
        request.setDeliveryAddressId(getAddress().getId());
        when(customOrderMapper.map(any(ApiUpdateOrderRequest.class))).thenReturn(order);
        when(orderService.updateOrder(eq(orderId), eq(request.getDeliveryAddressId()), any(Order.class))).thenReturn(order);
        when(customOrderMapper.map(order)).thenReturn(new ApiOrderResponse());

        // Act
        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assertions
        verify(orderService).updateOrder(eq(orderId), eq(request.getDeliveryAddressId()), any(Order.class));
    }

    @Test
    void deleteOrderById_ShouldReturn200_WhenOrderExists() throws Exception {
        // Arrange
        UUID orderId = getOrder().getId();

        // Act
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk());

        // Assertions
        verify(orderService).deleteOrderById(orderId);
    }

    @Test
    void getOrdersByClientId_ShouldReturn200_WhenFilterIsValid() throws Exception {
        // Arrange
        when(orderService.getOrdersByFilter(any())).thenReturn(List.of(getOrder()));
        when(customOrderMapper.customMap(any())).thenReturn(List.of());

        // Act
        mockMvc.perform(get("/api/orders/by-client-filter").param("clientId", getClient().getId().toString()))
                .andExpect(status().isOk());

        // Assertions
        verify(orderService).getOrdersByFilter(any());
    }
}
