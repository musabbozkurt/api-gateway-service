package com.mb.modulith.delivery.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mb.modulith.delivery.enums.ParticipantType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiPersonRequest {

    @JsonIgnore
    private Long id;
    private String name;

    @Email
    private String email;

    private String registrationNumber;

    @NotNull
    @Schema(description = "Participant role", example = "CUSTOMER")
    private ParticipantType type;
}
