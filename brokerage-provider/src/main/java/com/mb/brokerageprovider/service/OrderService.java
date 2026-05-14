package com.mb.brokerageprovider.service;

import com.mb.brokerageprovider.data.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<Order> getAllOrders(Pageable pageable);

    Order findById(Long orderId);

    Order buyStockOrder(Order order);

    Order sellStockOrder(Order order);

    Order cancelOrderById(Long orderId);

    void updateOrder(Long orderId);
}
