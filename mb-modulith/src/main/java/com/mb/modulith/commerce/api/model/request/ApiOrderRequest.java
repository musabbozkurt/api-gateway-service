package com.mb.modulith.commerce.api.model.request;

import com.mb.modulith.commerce.enums.PilotesEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ApiOrderRequest {
    private UUID clientId;
    private String number;
    private UUID deliveryAddressId;
    private PilotesEnum pilotes;
    private BigDecimal orderTotal;

}
