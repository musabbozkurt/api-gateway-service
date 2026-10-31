package com.mb.modulith.playermarket.service.dto.team;

import com.mb.modulith.playermarket.service.dto.BaseDto;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
import com.neovisionaries.i18n.CountryCode;
import com.neovisionaries.i18n.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiTeamResponse extends BaseDto {
    private String name;
    private String shortName;
    private String website;
    private String email;
    private String clubColors;
    private CountryCode country;
    private List<ApiPlayerResponse> squad;
    private OffsetDateTime updateDateTime;
    private CurrencyCode currencyCode;
}
