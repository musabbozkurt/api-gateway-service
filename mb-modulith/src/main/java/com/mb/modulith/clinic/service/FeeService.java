package com.mb.modulith.clinic.service;

import java.math.BigDecimal;

public interface FeeService {

    BigDecimal calculatePenaltyFee(BigDecimal appointmentFee);
}
