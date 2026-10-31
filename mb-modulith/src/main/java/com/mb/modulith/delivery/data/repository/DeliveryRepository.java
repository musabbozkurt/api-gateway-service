package com.mb.modulith.delivery.data.repository;

import com.mb.modulith.delivery.data.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("""
            SELECT d FROM Delivery d
            WHERE d.deliveryMan.id = :deliveryManId
              AND d.id <> COALESCE(:excludeId, -1)
              AND d.startTime < :endTime
              AND d.endTime > :startTime
            """)
    List<Delivery> findOverlappingForDeliveryMan(@Param("deliveryManId") Long deliveryManId,
                                                 @Param("startTime") Instant startTime,
                                                 @Param("endTime") Instant endTime,
                                                 @Param("excludeId") Long excludeId);
}
