package com.mb.modulith.playermarket.service.dto.team;

import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.neovisionaries.i18n.CountryCode;
import com.neovisionaries.i18n.CurrencyCode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiTeamRequest {
    private String name;
    private String shortName;
    private String website;
    private String email;
    private String clubColors;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CountryCode country;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CurrencyCode currencyCode;

    private List<PlayerInfo> squad;
}
