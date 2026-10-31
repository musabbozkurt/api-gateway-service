package com.mb.modulith.commerce.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiClientOrderResponse {
    private UUID id;
    private Date createdDateTime;
    private boolean deleted;
    private UUID orderId;
    private String number;
    private UUID deliveryAddressId;
    private int pilotes;
    private BigDecimal orderTotal;
    private UUID clientId;
}
