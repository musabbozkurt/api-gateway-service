package com.mb.modulith.clinic.api.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    private Integer userId;
    private Integer doctorId;
    private Integer patientId;

}