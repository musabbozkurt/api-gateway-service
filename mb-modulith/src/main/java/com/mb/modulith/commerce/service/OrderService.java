package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.data.filters.ClientFilter;
import com.mb.modulith.commerce.data.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(UUID clientId, UUID deliveryAddressId, Order order);

    Order getOrderById(UUID id);

    Order updateOrder(UUID id, UUID deliveryAddressId, Order order);

    void deleteOrderById(UUID id);

    List<Order> getOrdersByFilter(ClientFilter clientFilter);
}
