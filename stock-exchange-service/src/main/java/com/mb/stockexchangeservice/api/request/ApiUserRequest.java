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
public class ApiUserRequest {

    @NotNull
    @Schema(description = "User first name", example = "Jack")
    private String firstName;

    @NotNull
    @Schema(description = "User last name", example = "Sparrow")
    private String lastName;

    @NotNull
    @Schema(description = "User username", example = "jack_sparrow")
    private String username;

    @NotNull
    @ToString.Exclude
    @Schema(description = "User password", example = "test1234")
    private String password;

    @NotNull
    @Schema(description = "User email", example = "jack.sparrow@gmail.com")
    private String email;

    @NotNull
    @Schema(description = "User phone number", example = "901233459867")
    private String phoneNumber;
}
