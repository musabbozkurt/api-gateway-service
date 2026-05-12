package com.mb.brokerageprovider.base;

import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.request.ApiStockRequest;
import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.data.entity.Order;
import com.mb.brokerageprovider.data.entity.Stock;
import com.mb.brokerageprovider.data.entity.User;
import com.mb.brokerageprovider.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseUnitTest {

    public static final List<ApiOrderRequest> orderRequests = new ArrayList<>();

    static {
        orderRequests.add(ApiOrderRequest.builder().userId(0L).productCode("APPLE").quantity(3L).build());
        orderRequests.add(ApiOrderRequest.builder().userId(2L).productCode("APPLE").quantity(3L).build());
        orderRequests.add(ApiOrderRequest.builder().userId(3L).productCode("APPLE").quantity(4L).build());
    }

    public static ApiOrderRequest getApiOrderRequest() {
        return ApiOrderRequest.builder().userId(10L).productCode("APPLE").quantity(10L).build();
    }

    public static Order getOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setCreatedDateTime(OffsetDateTime.now());
        order.setModifiedDateTime(OffsetDateTime.now());

        User user = getUser();
        order.setUser(user);
        order.setUserId(user.getId());

        order.setStatus(OrderStatus.COMPLETED);
        order.setProductCode("APPLE");
        order.setQuantity(5L);
        return order;
    }

    public static Order getOrder2() {
        Order order = new Order();
        order.setId(11L);
        order.setCreatedDateTime(OffsetDateTime.now());
        order.setModifiedDateTime(OffsetDateTime.now());

        User user = getUser();
        order.setUser(user);
        order.setUserId(user.getId());

        order.setProductCode("TESLA");
        order.setQuantity(10L);
        return order;
    }

    public static Order getOrder3() {
        Order order = new Order();
        order.setId(12L);
        order.setCreatedDateTime(OffsetDateTime.now());
        order.setModifiedDateTime(OffsetDateTime.now());

        User user = getUser();
        order.setUser(user);
        order.setUserId(2L);

        order.setProductCode("TESLA");
        order.setQuantity(5L);
        return order;
    }

    public static Order buyOrSellOrder() {
        Order order = new Order();
        order.setProductCode("APPLE");

        User user = getUser();
        order.setUser(user);
        order.setUserId(user.getId());

        order.setQuantity(2L);
        return order;
    }

    public static User getUser() {
        User user = new User();
        user.setId(2L);
        user.setCreatedDateTime(OffsetDateTime.now());
        user.setModifiedDateTime(OffsetDateTime.now());
        user.setName("Jack");
        user.setSurname("Hack");
        user.setUsername("jack_hack");
        user.setEmail("jack.hack@gmail.com");
        user.setPhoneNumber("1234567890");
        return user;
    }

    public static ApiUserRequest getApiUserRequest() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setName("Jack");
        apiUserRequest.setSurname("Hack");
        apiUserRequest.setUsername("jack_hack");
        apiUserRequest.setEmail("jack.hack@gmail.com");
        apiUserRequest.setPhoneNumber("1234567899");
        return apiUserRequest;
    }

    public static ApiUserRequest getApiUserRequest2() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setName("Jack");
        apiUserRequest.setSurname("Hack");
        apiUserRequest.setUsername("jack_hack");
        apiUserRequest.setEmail("jack.hack@gmail.com");
        apiUserRequest.setPhoneNumber("1234567890");
        return apiUserRequest;
    }

    public static ApiUserRequest getApiUserRequest3() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setEmail("jack.hack.new@gmail.com");
        return apiUserRequest;
    }

    public static List<Order> getOrders() {
        return List.of(getOrder(), getOrder2(), getOrder3());
    }

    public static Stock getStock() {
        Stock stock = new Stock();
        stock.setQuantity(10L);
        stock.setProductCode("APPLE");
        return stock;
    }

    public static List<Stock> getStocks() {
        return List.of(getStock());
    }

    public static ApiStockRequest getApiStockRequest() {
        return ApiStockRequest.builder().productCode("MICROSOFT").quantity(10L).build();
    }

    public static ApiStockRequest getApiStockRequest2() {
        return ApiStockRequest.builder().productCode("APPLE").quantity(10L).build();
    }
}
