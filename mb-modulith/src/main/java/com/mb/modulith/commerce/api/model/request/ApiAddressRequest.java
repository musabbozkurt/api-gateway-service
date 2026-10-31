package com.mb.modulith.commerce.api.model.request;

import com.neovisionaries.i18n.CountryCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApiAddressRequest {

    @NotEmpty(message = "{clientId.notEmpty}")
    private UUID clientId;
    private String street;
    private String postcode;
    private String city;
    @NotNull
    private CountryCode country;
}
