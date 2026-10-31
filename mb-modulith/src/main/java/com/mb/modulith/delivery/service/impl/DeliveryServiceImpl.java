package com.mb.modulith.delivery.service.impl;

import com.mb.modulith.delivery.api.response.ApiTopCourierResponse;
import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.data.repository.DeliveryRepository;
import com.mb.modulith.delivery.data.repository.PersonRepository;
import com.mb.modulith.delivery.enums.ParticipantType;
import com.mb.modulith.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final PersonRepository personRepository;

    private static ApiTopCourierResponse toTopCourierResponse(Long deliveryManId, List<Delivery> deliveries) {
        Person courier = deliveries.getFirst().getDeliveryMan();
        long maxPrice = deliveries.stream()
                .map(Delivery::getPrice)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        double averageCommission = deliveries.stream()
                .map(Delivery::getComission)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0d);
        return ApiTopCourierResponse.builder()
                .deliveryManId(deliveryManId)
                .deliveryManName(courier.getName())
                .maxOrderPrice(maxPrice)
                .averageCommission(averageCommission)
                .build();
    }

    private static void validateParticipantTypes(Delivery delivery) {
        if (delivery.getDeliveryMan().getType() != ParticipantType.DELIVERY_MAN) {
            throw new IllegalArgumentException("Delivery man must have type DELIVERY_MAN");
        }
        if (delivery.getCustomer().getType() != ParticipantType.CUSTOMER) {
            throw new IllegalArgumentException("Customer must have type CUSTOMER");
        }
    }

    private static void applyCommissionIfMissing(Delivery delivery) {
        if (delivery.getComission() != null) {
            return;
        }
        Long price = delivery.getPrice();
        Long distance = delivery.getDistance();
        if (price == null || distance == null) {
            return;
        }
        delivery.setComission(Math.round(price * 0.05d + distance * 0.5d));
    }

    @Override
    public Delivery save(Delivery delivery) {
        resolveParticipants(delivery);
        validateParticipantTypes(delivery);
        ensureNoOverlappingDeliveries(delivery);
        applyCommissionIfMissing(delivery);
        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery findById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId).orElse(null);
    }

    @Override
    public List<ApiTopCourierResponse> findTopCouriersByMaxOrderPrice(Instant from, Instant to, int limit) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Invalid time interval");
        }
        return deliveryRepository.findAll().stream()
                .filter(d -> !d.getStartTime().isBefore(from) && !d.getStartTime().isAfter(to))
                .filter(d -> d.getDeliveryMan() != null)
                .collect(Collectors.groupingBy(d -> d.getDeliveryMan().getId()))
                .entrySet().stream()
                .map(entry -> toTopCourierResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ApiTopCourierResponse::getMaxOrderPrice).reversed())
                .limit(limit)
                .toList();
    }

    private void resolveParticipants(Delivery delivery) {
        if (delivery.getDeliveryMan() == null && delivery.getDeliveryManId() != null) {
            delivery.setDeliveryMan(requirePerson(delivery.getDeliveryManId()));
        }
        if (delivery.getCustomer() == null && delivery.getCustomerId() != null) {
            delivery.setCustomer(requirePerson(delivery.getCustomerId()));
        }
        if (delivery.getDeliveryMan() == null || delivery.getCustomer() == null) {
            throw new IllegalArgumentException("Delivery man and customer must be registered participants");
        }
    }

    private Person requirePerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown participant id: " + id));
    }

    private void ensureNoOverlappingDeliveries(Delivery delivery) {
        List<Delivery> overlapping = deliveryRepository.findOverlappingForDeliveryMan(
                delivery.getDeliveryMan().getId(),
                delivery.getStartTime(),
                delivery.getEndTime(),
                delivery.getId());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Delivery man already has an overlapping delivery");
        }
    }
}
