package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.filters.ApiClientFilter;
import com.mb.modulith.commerce.api.model.request.ApiOrderRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateOrderRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientOrderResponse;
import com.mb.modulith.commerce.api.model.response.ApiOrderResponse;
import com.mb.modulith.commerce.data.filters.ClientFilter;
import com.mb.modulith.commerce.mapping.CustomOrderMapper;
import com.mb.modulith.commerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final CustomOrderMapper customOrderMapper;
    private final OrderService orderService;

    /**
     * Creates order.
     *
     * @param apiOrderRequest to create order.
     */
    @PostMapping
    @PreAuthorize("hasRole('CREATE_ORDER')")
    @Operation(summary = "Creates order.")
    public ResponseEntity<ApiOrderResponse> createOrder(@RequestBody ApiOrderRequest apiOrderRequest) {
        log.info("Received a request to create order. ApiAddressRequest: {}", apiOrderRequest);
        return ResponseEntity.ok().body(customOrderMapper.map(orderService.createOrder(apiOrderRequest.getClientId(),
                apiOrderRequest.getDeliveryAddressId(), customOrderMapper.map(apiOrderRequest))));
    }

    /**
     * returns order by Id
     *
     * @param id order id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GET_ORDER')")
    @Operation(summary = "returns order by Id.")
    public ResponseEntity<ApiOrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(customOrderMapper.map(orderService.getOrderById(id)));
    }

    /**
     * Updates order.
     *
     * @param id                    order id.
     * @param apiUpdateOrderRequest to update order.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UPDATE_ORDER')")
    @Operation(summary = "Updates order.")
    public ResponseEntity<ApiOrderResponse> updateOrder(@PathVariable UUID id, @RequestBody ApiUpdateOrderRequest apiUpdateOrderRequest) {
        log.info("Received a request to update order. ApiAddressRequest: {}", apiUpdateOrderRequest);
        return ResponseEntity.ok().body(customOrderMapper.map(orderService.updateOrder(id, apiUpdateOrderRequest.getDeliveryAddressId(), customOrderMapper.map(apiUpdateOrderRequest))));
    }

    /**
     * delete order by id
     *
     * @param id order id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DELETE_ORDER')")
    @Operation(summary = "deletes order by id")
    public void deleteOrderById(@PathVariable UUID id) {
        log.info("Received a request to delete order. id: {}", id);
        orderService.deleteOrderById(id);
    }

    /**
     * returns orders by filter
     */
    @GetMapping("/by-client-filter")
    @PreAuthorize("hasRole('GET_ORDER')")
    @Operation(summary = "returns orders by clientId.")
    public ResponseEntity<List<ApiClientOrderResponse>> getOrdersByClientId(@Valid ApiClientFilter apiClientFilter) {
        ClientFilter clientFilter = customOrderMapper.map(apiClientFilter);
        return ResponseEntity.ok().body(customOrderMapper.customMap(orderService.getOrdersByFilter(clientFilter)));
    }

}
