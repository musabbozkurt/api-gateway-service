package com.mb.modulith.commerce.api.model.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApiClientFilter {
    @Schema(description = "Mobile number to filter by", name = "telephone")
    private String telephone;

    @Schema(description = "First name to filter by", name = "firstName")
    private String firstName;

    @Schema(description = "Last name to filter by", name = "lastName")
    private String lastName;

    @Schema(description = "Email to filter by", name = "email")
    private String email;

    @Schema(description = "ClientId to filter by", name = "clientId")
    private UUID clientId;
}

