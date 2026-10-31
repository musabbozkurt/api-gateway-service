package com.mb.modulith.commerce.api.model.request;

import com.neovisionaries.i18n.CountryCode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiUpdateAddressRequest {
    private String street;
    private String postcode;
    private String city;
    @NotNull
    private CountryCode country;
}
