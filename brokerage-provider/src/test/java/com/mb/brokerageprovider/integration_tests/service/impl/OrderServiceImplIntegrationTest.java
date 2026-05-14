package com.mb.brokerageprovider.integration_tests.service.impl;

import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.config.IntegrationTestConfiguration;
import com.mb.brokerageprovider.data.entity.Order;
import com.mb.brokerageprovider.enums.OrderStatus;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@ActiveProfiles("test-containers")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
public class OrderServiceImplIntegrationTest extends BaseUnitTest {

    @Autowired
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // Perform necessary setup for the test
        orderService.buyStockOrder(buyOrSellOrder());
        orderService.sellStockOrder(buyOrSellOrder());
    }

    @Test
    @org.junit.jupiter.api.Order(value = 1)
    void testBuyStockOrder() {
        // Initialize with required properties
        Order order = buyOrSellOrder();

        // Perform necessary setup for the test
        Order savedOrder = orderService.buyStockOrder(order);

        // Assertions
        assertNotNull(savedOrder);
        assertEquals(OrderStatus.INITIATED, savedOrder.getStatus());
        assertEquals(savedOrder.getUser().getId(), order.getUserId());
    }

    @Test
    @org.junit.jupiter.api.Order(value = 2)
    void testBuyStockOrder_ShouldFail_WhenOrderAttributesAreEmpty() {
        // Initialize with required properties
        Order order = new Order();

        // Perform necessary setup for the test
        // Assertions
        assertThrows(BaseException.class, () -> orderService.buyStockOrder(order));
    }

    @Test
    @org.junit.jupiter.api.Order(value = 3)
    void testSellStockOrder() {
        // Initialize with required properties
        Order order = buyOrSellOrder();

        // Perform necessary setup for the test
        Order savedOrder = orderService.sellStockOrder(order);

        // Assertions
        assertNotNull(savedOrder);
        assertEquals(OrderStatus.INITIATED, savedOrder.getStatus());
        assertEquals(savedOrder.getUser().getId(), order.getUserId());
    }

    @Test
    @org.junit.jupiter.api.Order(value = 4)
    void testSellStockOrder_ShouldFail_WhenOrderAttributesAreEmpty() {
        // Initialize with required properties
        Order order = new Order();

        // Perform necessary setup for the test
        // Assertions
        assertThrows(BaseException.class, () -> orderService.sellStockOrder(order));
    }

    @Test
    @org.junit.jupiter.api.Order(value = 5)
    void testGetAllOrders() {
        // Perform necessary setup for the test
        Page<Order> orders = orderService.getAllOrders(Pageable.unpaged());

        List<Order> orderList = orders.getContent();

        assertNotNull(orders);
        then(orderList).isNotEmpty();
        assertThat(orderList.getFirst().getQuantity()).isNotZero();
        assertThat(orderList.getFirst().getProductCode()).isNotEmpty();
    }

    @Test
    @org.junit.jupiter.api.Order(value = 6)
    void testCancelOrderById_ShouldFail_WhenOrderIsNotFound() {
        Long orderId = 1L;

        // Perform necessary setup for the test
        assertThrows(BaseException.class, () -> orderService.cancelOrderById(orderId));
    }

    @Test
    @org.junit.jupiter.api.Order(value = 7)
    void testFindById() {
        // Create an order first to ensure it exists
        Order createdOrder = orderService.buyStockOrder(buyOrSellOrder());
        Long orderId = createdOrder.getId();

        // Perform necessary setup for the test
        Order order = orderService.findById(orderId);

        // Assertions
        assertNotNull(order);
        assertEquals(OrderStatus.INITIATED, order.getStatus());
        assertEquals(orderId, order.getId());
    }

    @Test
    @org.junit.jupiter.api.Order(value = 8)
    void testFindById_ShouldFail_WhenOrderIsNotFound() {
        Long orderId = 50L;

        // Perform necessary setup for the test
        assertThrows(BaseException.class, () -> orderService.findById(orderId));
    }

    @Test
    @org.junit.jupiter.api.Order(value = 9)
    void testUpdateOrder_ShouldFail_WhenOrderIsNotFound() {
        Long orderId = 1L;

        // Perform necessary setup for the test
        assertThrows(BaseException.class, () -> orderService.updateOrder(orderId));
    }
}