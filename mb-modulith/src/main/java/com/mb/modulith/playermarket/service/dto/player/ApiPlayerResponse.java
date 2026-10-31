package com.mb.modulith.playermarket.service.dto.player;

import com.mb.modulith.playermarket.enums.PositionType;
import com.mb.modulith.playermarket.service.dto.BaseDto;
import com.neovisionaries.i18n.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiPlayerResponse extends BaseDto {
    private String name;
    private String surname;
    private PositionType position;
    private Integer jerseyNumber;
    private String dateOfBirth;
    private String nationality;
    private String contractUntil;
    private BigDecimal marketValue;
    private CurrencyCode marketValueCurrency;
    private OffsetDateTime updateDateTime;
}
