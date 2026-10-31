package com.mb.modulith.email.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiEmailRequest {

    @NotNull
    private String emailAddress;

    private boolean blockedDomain;

}
