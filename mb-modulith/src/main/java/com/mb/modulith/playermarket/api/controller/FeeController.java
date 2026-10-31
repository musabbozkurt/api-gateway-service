package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.service.FeeService;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeRequest;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    /**
     * calculate Fee By team_info id.
     *
     * @param teamId team_info id.
     */
    @GetMapping("/fee/{teamId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "calculate Fee By team_info id.")
    public ResponseEntity<ApiFeeResponse> calculateFeeByTeamId(@PathVariable("teamId") UUID teamId, ApiFeeRequest apiFeeRequest) {
        return ResponseEntity.ok().body(feeService.calculateFeeByTeamId(teamId, apiFeeRequest));
    }
}
