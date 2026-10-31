package com.mb.modulith.playermarket.service.dto.player;

import com.mb.modulith.playermarket.enums.PositionType;
import com.neovisionaries.i18n.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiPlayerRequest {
    private String name;
    private String surname;
    private PositionType position;
    private Integer jerseyNumber;
    private String dateOfBirth;
    private String nationality;
    private String contractUntil;
    private BigDecimal marketValue;
    private CurrencyCode marketValueCurrency;
}
