package com.mb.modulith.clinic.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAppointmentRequest {

    private Integer doctorId;
    private OffsetDateTime endDate;
    private Integer patientId;
    private OffsetDateTime startDate;
}
