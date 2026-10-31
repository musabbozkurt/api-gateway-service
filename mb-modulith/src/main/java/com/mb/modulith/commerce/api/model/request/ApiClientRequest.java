package com.mb.modulith.commerce.api.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiClientRequest {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String telephone;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String email;
}
