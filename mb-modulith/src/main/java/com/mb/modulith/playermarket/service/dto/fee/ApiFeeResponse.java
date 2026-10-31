package com.mb.modulith.playermarket.service.dto.fee;

import com.neovisionaries.i18n.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ApiFeeResponse {
    private BigDecimal contractFee;
    private CurrencyCode contractFeeCurrency;
}
