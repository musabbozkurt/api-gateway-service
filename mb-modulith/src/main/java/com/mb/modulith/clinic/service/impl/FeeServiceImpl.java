package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.config.properties.ClinicProperties;
import com.mb.modulith.clinic.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Service("clinicFeeServiceImpl")
public class FeeServiceImpl implements FeeService {

    private final ClinicProperties clinicProperties;

    @Override
    public BigDecimal calculatePenaltyFee(BigDecimal appointmentFee) {
        BigDecimal penaltyFee = appointmentFee.multiply(clinicProperties.getPenaltyPercentage());
        return appointmentFee.subtract(penaltyFee);
    }
}
