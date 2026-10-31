package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiDoctorRequest;
import com.mb.modulith.clinic.api.response.ApiDoctorResponse;
import com.mb.modulith.clinic.mapper.DoctorMapper;
import com.mb.modulith.clinic.service.DoctorService;
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

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get doctor by id if doctorId is in the header otherwise it will return all doctors.")
    public ResponseEntity<Object> getDoctorById() {
        if (Objects.nonNull(ContextHolder.get().getDoctorId())) {
            log.info("Received a request to get doctor by id. getDoctorById - doctorId: {}", ContextHolder.get().getDoctorId());
            return ResponseEntity.ok(doctorMapper.map(doctorService.getDoctorById(ContextHolder.get().getDoctorId())));
        }
        log.info("Received a request to get all doctors. getAllDoctors.");
        return ResponseEntity.ok(doctorMapper.map(doctorService.getAllDoctors()));
    }

    @PostMapping("/doctor")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create doctor.")
    public ResponseEntity<ApiDoctorResponse> createDoctor(@RequestBody ApiDoctorRequest apiDoctorRequest) {
        log.info("Received a request to create doctor. createDoctor - apiDoctorRequest: {}", apiDoctorRequest);
        return new ResponseEntity<>(doctorMapper.map(doctorService.createDoctor(doctorMapper.map(apiDoctorRequest))), HttpStatus.CREATED);
    }

    @PutMapping("/doctor")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update doctor.")
    public ResponseEntity<ApiDoctorResponse> updateDoctorById(@RequestBody ApiDoctorRequest apiDoctorRequest) {
        log.info("Received a request to update doctor by id. updateDoctorById - apiDoctorRequest: {}", apiDoctorRequest);
        return new ResponseEntity<>(doctorMapper.map(doctorService.updateDoctorById(ContextHolder.get().getDoctorId(), doctorMapper.map(apiDoctorRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/doctor")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Delete doctor by id.")
    public ResponseEntity<String> deleteDoctorById() {
        log.info("Received a request to delete doctor by id. deleteDoctorById - doctorId: {}", ContextHolder.get().getDoctorId());
        doctorService.deleteDoctorById(ContextHolder.get().getDoctorId());
        return new ResponseEntity<>("Doctor deleted successfully.", HttpStatus.OK);
    }

}
