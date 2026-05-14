package com.mb.stockexchangeservice.api.response;

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

    @Schema(description = "User first name")
    private String firstName;

    @Schema(description = "User last name")
    private String lastName;

    @Schema(description = "User username")
    private String username;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User phone number")
    private String phoneNumber;
}
