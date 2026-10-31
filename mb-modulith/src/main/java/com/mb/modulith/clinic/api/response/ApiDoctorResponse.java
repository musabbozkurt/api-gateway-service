package com.mb.modulith.clinic.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDoctorResponse {

    private int id;
    private String name;
    private String surname;
    private BigDecimal perHourFee;
}
