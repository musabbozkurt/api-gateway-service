package com.mb.modulith.delivery.service;

import com.mb.modulith.delivery.api.response.ApiTopCourierResponse;
import com.mb.modulith.delivery.data.model.Delivery;

import java.time.Instant;
import java.util.List;

public interface DeliveryService {

    Delivery save(Delivery delivery);

    Delivery findById(Long deliveryId);

    List<ApiTopCourierResponse> findTopCouriersByMaxOrderPrice(Instant from, Instant to, int limit);
}
