package com.mb.brokerageprovider.mapper;

import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.data.entity.Order;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderMapperTest extends BaseUnitTest {

    OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void map_OrderToApiOrderResponse_ShouldSucceed() {
        // arrange
        Order order = getOrder();

        // act
        ApiOrderResponse result = orderMapper.map(order);

        // assertion

        assertEquals(order.getQuantity(), result.getQuantity());
        assertEquals(order.getUser().getId(), result.getUser().getId());
        assertEquals(order.getProductCode(), result.getProductCode());
    }

    @Test
    void map_ListOfOrderToListOfApiOrderResponse_ShouldSucceed() {
        // arrange
        List<Order> apiOrderRequest = getOrders();

        // act
        List<ApiOrderResponse> result = orderMapper.map(apiOrderRequest);

        // assertion
        assertEquals(apiOrderRequest.getFirst().getQuantity(), result.getFirst().getQuantity());
        assertEquals(apiOrderRequest.getFirst().getUser().getId(), result.getFirst().getUser().getId());
        assertEquals(apiOrderRequest.getFirst().getProductCode(), result.getFirst().getProductCode());
    }

    @Test
    void map_ApiOrderRequestToOrder_ShouldSucceed() {
        // arrange
        ApiOrderRequest apiOrderRequest = getApiOrderRequest();

        // act
        Order result = orderMapper.map(apiOrderRequest);

        // assertion
        assertEquals(apiOrderRequest.getQuantity(), result.getQuantity());
        assertEquals(apiOrderRequest.getUserId(), result.getUserId());
        assertEquals(apiOrderRequest.getProductCode(), result.getProductCode());
    }

    @Test
    void map_OrderWithPaginationToApiOrderResponsePagination_ShouldSucceed() {
        // arrange
        List<Order> orders = getOrders();

        final Page<Order> orderPage = new PageImpl<>(orders, Pageable.ofSize(10), orders.size());

        // act
        Page<ApiOrderResponse> result = orderMapper.map(orderPage);

        // assertion
        assertEquals(orderPage.getSize(), result.getSize());
        assertEquals(orderPage.getContent().getFirst().getUser().getId(), result.getContent().getFirst().getUser().getId());
        assertEquals(orderPage.getContent().getFirst().getQuantity(), result.getContent().getFirst().getQuantity());
        assertEquals(orderPage.getContent().getFirst().getStatus(), result.getContent().getFirst().getStatus());
    }
}
