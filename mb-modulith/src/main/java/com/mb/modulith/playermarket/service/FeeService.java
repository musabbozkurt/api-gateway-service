package com.mb.modulith.playermarket.service;

import com.mb.modulith.playermarket.service.dto.fee.ApiFeeRequest;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeResponse;

import java.util.UUID;

public interface FeeService {

    ApiFeeResponse calculateFeeByTeamId(UUID teamId, ApiFeeRequest apiFeeRequest);
}
