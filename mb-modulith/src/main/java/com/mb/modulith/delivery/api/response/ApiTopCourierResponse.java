package com.mb.modulith.delivery.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTopCourierResponse {

    private Long deliveryManId;
    private String deliveryManName;
    private Long maxOrderPrice;
    private Double averageCommission;
}
