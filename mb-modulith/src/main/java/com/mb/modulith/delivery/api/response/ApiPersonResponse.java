package com.mb.modulith.delivery.api.response;

import com.mb.modulith.delivery.enums.ParticipantType;
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
public class ApiPersonResponse {

    private Long id;
    private String name;
    private String email;
    private String registrationNumber;
    private ParticipantType type;
}
