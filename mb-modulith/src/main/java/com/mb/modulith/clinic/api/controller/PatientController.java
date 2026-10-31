package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiPatientRequest;
import com.mb.modulith.clinic.api.response.ApiPatientResponse;
import com.mb.modulith.clinic.mapper.PatientMapper;
import com.mb.modulith.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @PostMapping("/patient")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create patient.")
    public ResponseEntity<ApiPatientResponse> createPatient(@RequestBody ApiPatientRequest apiPatientRequest) {
        log.info("Received a request to create patient. createPatient - apiPatientRequest: {}", apiPatientRequest);
        return new ResponseEntity<>(patientMapper.map(patientService.createPatient(patientMapper.map(apiPatientRequest))), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all patients.")
    public List<ApiPatientResponse> getAllPatients() {
        log.info("Received a request to get all patients. getAllPatients.");
        return patientMapper.map(patientService.getAllPatients());
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get patient by id. patientId should be sent in the header.")
    public ResponseEntity<ApiPatientResponse> getPatientById() {
        log.info("Received a request to get patient by id. getPatientById - patientId: {}", ContextHolder.get().getPatientId());
        return new ResponseEntity<>(patientMapper.map(patientService.getPatientById(ContextHolder.get().getPatientId())), HttpStatus.OK);
    }

    @PutMapping("/patient")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update patient by id. patientId should be sent in the header.")
    public ResponseEntity<ApiPatientResponse> updatePatientById(@RequestBody ApiPatientRequest apiPatientRequest) {
        log.info("Received a request to update patient by id. updatePatientById - patientId: {}, apiPatientRequest: {}", ContextHolder.get().getPatientId(), apiPatientRequest);
        return new ResponseEntity<>(patientMapper.map(patientService.updatePatientById(ContextHolder.get().getPatientId(), patientMapper.map(apiPatientRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/patient")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Delete patient by id. patientId should be sent in the header.")
    public ResponseEntity<String> deletePatientById() {
        log.info("Received a request to delete patient by id. deletePatientById - patientId: {}", ContextHolder.get().getPatientId());
        patientService.deletePatientById(ContextHolder.get().getPatientId());
        return new ResponseEntity<>("Patient deleted successfully.", HttpStatus.OK);
    }
}
