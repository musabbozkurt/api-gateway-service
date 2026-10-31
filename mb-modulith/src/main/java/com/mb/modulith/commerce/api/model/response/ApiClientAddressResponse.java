package com.mb.modulith.commerce.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neovisionaries.i18n.CountryCode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiClientAddressResponse {
    @JsonProperty("deliveryAddressId")
    private UUID id;
    private UUID clientId;
    private String street;
    private String postcode;
    private String city;
    @NotNull
    private CountryCode country;
}
