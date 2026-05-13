package com.mb.stockexchangeservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUserAuthRequest {

    @NotNull
    @Schema(description = "Username of the user", example = "admin_user")
    private String username;

    @NotNull
    @ToString.Exclude
    @Schema(description = "Password of the user", example = "test1234")
    private String password;
}
