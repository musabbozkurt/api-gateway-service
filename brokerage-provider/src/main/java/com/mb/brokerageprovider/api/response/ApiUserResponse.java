package com.mb.brokerageprovider.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUserResponse {

    @Schema(description = "User id")
    private Long id;

    @Schema(description = "User creation date")
    private OffsetDateTime createdDateTime;

    @Schema(description = "User modified date")
    private OffsetDateTime modifiedDateTime;

    @Schema(description = "User name")
    private String name;

    @Schema(description = "User surname")
    private String surname;

    @Schema(description = "User username")
    private String username;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User phone number")
    private String phoneNumber;
}
