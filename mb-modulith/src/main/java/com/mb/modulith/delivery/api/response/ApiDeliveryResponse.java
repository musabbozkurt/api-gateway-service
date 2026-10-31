package com.mb.modulith.delivery.api.response;

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
public class ApiDeliveryResponse {

    private Long id;

    private Instant startTime;

    private Instant endTime;

    private Long distance;

    private Long price;

    private Long comission;

    private ApiPersonResponse deliveryMan;

    private ApiPersonResponse customer;
}
