package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiAppointmentRequest;
import com.mb.modulith.clinic.api.response.ApiAppointmentResponse;
import com.mb.modulith.clinic.mapper.AppointmentMapper;
import com.mb.modulith.clinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get user appointments by userId.")
    public List<ApiAppointmentResponse> getAllAppointmentsByUserId() {
        log.info("Received a request to get all appointments by user id. getAllAppointmentsByUserId - userId: {}.", ContextHolder.get().getUserId());
        return appointmentMapper.map(appointmentService.getAllAppointmentsByUserId(ContextHolder.get().getUserId()));
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create appointment.")
    public ResponseEntity<ApiAppointmentResponse> createAppointment(@RequestBody ApiAppointmentRequest apiAppointmentRequest) {
        log.info("Received a request to create appointment. createAppointment - apiAppointmentRequest: {}.", apiAppointmentRequest);
        return new ResponseEntity<>(appointmentMapper.map(appointmentService.createAppointment(appointmentMapper.map(apiAppointmentRequest))), HttpStatus.CREATED);
    }

    @PutMapping("/appointments")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update appointment.")
    public ResponseEntity<ApiAppointmentResponse> cancelAppointmentById(@RequestParam("appointmentId") int appointmentId) {
        log.info("Received a request to create cancel appointment by id. cancelAppointmentById - appointmentId: {}.", appointmentId);
        return new ResponseEntity<>(appointmentMapper.map(appointmentService.cancelAppointmentById(appointmentId)), HttpStatus.OK);
    }
}
