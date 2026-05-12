package com.mb.brokerageprovider.service.impl;

import com.mb.brokerageprovider.data.entity.Order;
import com.mb.brokerageprovider.data.entity.Stock;
import com.mb.brokerageprovider.data.entity.User;
import com.mb.brokerageprovider.data.repository.OrderRepository;
import com.mb.brokerageprovider.enums.OrderStatus;
import com.mb.brokerageprovider.enums.OrderType;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.queue.ProducerService;
import com.mb.brokerageprovider.service.OrderService;
import com.mb.brokerageprovider.service.StockService;
import com.mb.brokerageprovider.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final StockService stockService;
    private final ProducerService producerService;

    @Value("${spring.kafka.topics.topic1}")
    private String orderTopic;

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new BaseException(BrokerageProviderErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional
    public Order buyStockOrder(Order order) {
        Stock stock = stockService.getStockByProductCode(order.getProductCode());
        order.setType(OrderType.BUY);
        Long availableStocks = stock.getQuantity();
        if (stock.getQuantity() >= order.getQuantity()) {
            // Process the purchase
            availableStocks -= order.getQuantity();
            stock.setQuantity(availableStocks);
            return getOrder(order, stock);
        } else {
            throw new BaseException(BrokerageProviderErrorCode.INSUFFICIENT_STOCK);
        }
    }

    @Override
    @Transactional
    public Order sellStockOrder(Order order) {
        Stock stock = stockService.getStockByProductCode(order.getProductCode());
        order.setType(OrderType.SELL);
        // Process the sell request
        stock.setQuantity(stock.getQuantity() + order.getQuantity());
        return getOrder(order, stock);
    }

    @Override
    public Order cancelOrderById(Long orderId) {
        Order order = findById(orderId);
        if (!List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED).contains(order.getStatus())) {
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        } else {
            throw new BaseException(BrokerageProviderErrorCode.ORDER_CAN_NOT_BE_UPDATED);
        }
    }

    @Override
    public void updateOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(BrokerageProviderErrorCode.ORDER_NOT_FOUND));
        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new BaseException(BrokerageProviderErrorCode.ORDER_CAN_NOT_BE_UPDATED);
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    private Order getOrder(Order order, Stock stock) {
        stockService.updateStockById(stock.getId(), stock);
        User userById = userService.getUserById(order.getUserId());
        order.setUser(userById);
        Order savedOrder = orderRepository.save(order);
        producerService.publishMessage(orderTopic, savedOrder.getId().toString());
        return savedOrder;
    }
}
