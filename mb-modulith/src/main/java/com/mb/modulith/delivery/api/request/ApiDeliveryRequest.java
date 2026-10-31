package com.mb.modulith.delivery.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mb.modulith.delivery.data.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiDeliveryRequest {

    @JsonIgnore
    private Long id;

    private Instant startTime;

    private Instant endTime;

    private Long distance;

    private Long price;

    private Long comission;

    private Long deliveryManId;

    private Long customerId;

    @JsonIgnore
    private Person deliveryMan;

    @JsonIgnore
    private Person customer;
}
