package com.mb.brokerageprovider.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUserRequest {

    @Schema(description = "User name", example = "Jack")
    private String name;

    @Schema(description = "User surname", example = "Sparrow")
    private String surname;

    @Schema(description = "User username", example = "jack_sparrow")
    private String username;

    @Schema(description = "User email", example = "jack.sparrow@gmail.com")
    private String email;

    @Schema(description = "User phone number", example = "901233459867")
    private String phoneNumber;
}
