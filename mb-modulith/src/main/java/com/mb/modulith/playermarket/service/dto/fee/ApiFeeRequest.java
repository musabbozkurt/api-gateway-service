package com.mb.modulith.playermarket.service.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ApiFeeRequest {
    private int age;
    private BigDecimal teamCommission;
    private int monthOfExpirence;
}
