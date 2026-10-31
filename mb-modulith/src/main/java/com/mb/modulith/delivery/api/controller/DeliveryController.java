package com.mb.modulith.delivery.api.controller;

import com.mb.modulith.delivery.api.request.ApiDeliveryRequest;
import com.mb.modulith.delivery.api.response.ApiTopCourierResponse;
import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.mapper.DeliveryMapper;
import com.mb.modulith.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryMapper deliveryMapper;

    @PostMapping(path = "/api/delivery")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Record a completed delivery")
    public ResponseEntity<Delivery> createNewDelivery(@RequestBody ApiDeliveryRequest apiDeliveryRequest) {
        return ResponseEntity.ok(deliveryService.save(deliveryMapper.map(apiDeliveryRequest)));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(path = "/api/delivery/top-couriers")
    @Operation(summary = "Top couriers by max order price in an interval with average commission")
    public ResponseEntity<List<ApiTopCourierResponse>> getTopCouriers(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                                                      @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(deliveryService.findTopCouriersByMaxOrderPrice(from, to, limit));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get delivery by id")
    @GetMapping(path = "/api/delivery/{delivery-id}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable(name = "delivery-id") Long deliveryId) {
        Delivery delivery = deliveryService.findById(deliveryId);
        if (delivery != null) {
            return ResponseEntity.ok(delivery);
        }
        return ResponseEntity.notFound().build();
    }
}
