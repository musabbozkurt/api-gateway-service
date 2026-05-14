package com.mb.brokerageprovider.api.controller;

import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.mapper.OrderMapper;
import com.mb.brokerageprovider.service.OrderService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Observed
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/")
    @Observed(name = "getAllOrders")
    @Operation(description = "Get all orders.")
    public ResponseEntity<Page<ApiOrderResponse>> getAllOrders(Pageable pageable) {
        log.info("Received a request to get all orders. getAllOrders - pageSize: {}, page: {}", pageable.getPageSize(), pageable.getPageNumber());
        return new ResponseEntity<>(orderMapper.map(orderService.getAllOrders(pageable)), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    @Observed(name = "getOrderById")
    @Operation(description = "Get order by id.")
    public ResponseEntity<ApiOrderResponse> getOrderById(@PathVariable Long orderId) {
        log.info("Received a request to get order by id. getOrderById - orderId: {}", orderId);
        return new ResponseEntity<>(orderMapper.map(orderService.findById(orderId)), HttpStatus.ACCEPTED);
    }

    @PostMapping("/buy")
    @Observed(name = "buyStockOrder")
    @Operation(description = "Buy stock order.")
    public ResponseEntity<ApiOrderResponse> buyStockOrder(@RequestBody ApiOrderRequest apiOrderRequest) {
        log.info("Received a request to buy stock order. buyStockOrder - apiOrderRequest: {}.", apiOrderRequest);
        return new ResponseEntity<>(orderMapper.map(orderService.buyStockOrder(orderMapper.map(apiOrderRequest))), HttpStatus.ACCEPTED);
    }

    @PostMapping("/sell")
    @Observed(name = "sellStockOrder")
    @Operation(description = "Buy stock order.")
    public ResponseEntity<ApiOrderResponse> sellStockOrder(@RequestBody ApiOrderRequest apiOrderRequest) {
        log.info("Received a request to sell stock order. sellStockOrder - apiOrderRequest: {}.", apiOrderRequest);
        return new ResponseEntity<>(orderMapper.map(orderService.sellStockOrder(orderMapper.map(apiOrderRequest))), HttpStatus.ACCEPTED);
    }

    @PutMapping("/cancel/{orderId}")
    @Observed(name = "cancelOrderById")
    @Operation(description = "Cancel order by id.")
    public ResponseEntity<ApiOrderResponse> cancelOrderById(@PathVariable Long orderId) {
        log.info("Received a request to create cancel order by id. cancelOrderById - orderId: {}.", orderId);
        return new ResponseEntity<>(orderMapper.map(orderService.cancelOrderById(orderId)), HttpStatus.ACCEPTED);
    }
}
