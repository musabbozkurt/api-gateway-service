package com.mb.modulith.clinic.api.response;

import com.mb.modulith.clinic.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAppointmentResponse {

    private int id;

    private ApiDoctorResponse doctor;

    private OffsetDateTime endDate;

    private BigDecimal fee;

    private ApiPatientResponse patient;

    private BigDecimal refundedFee;

    private OffsetDateTime startDate;

    private AppointmentStatus status;

}
