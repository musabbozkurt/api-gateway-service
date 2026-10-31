package com.mb.modulith.email.api.controller;

import com.mb.modulith.email.api.dto.request.ApiEmailRequest;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.mapping.EmailMapper;
import com.mb.modulith.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {

    private final EmailService emailService;
    private final EmailMapper emailMapper;

    @GetMapping("/emails")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get list of emails")
    public ResponseEntity<List<ApiEmailResponse>> getAllEmails() {
        return ResponseEntity.ok(emailMapper.map(emailService.findAll()));
    }

    @GetMapping("/emails/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get email by id")
    public ResponseEntity<ApiEmailResponse> getEmailById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(emailMapper.map(emailService.findById(id)));
    }

    @PostMapping("/emails")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create new email")
    public ResponseEntity<ApiEmailResponse> createEmail(@RequestBody ApiEmailRequest emailRequest) {
        return ResponseEntity.ok(emailMapper.map(emailService.save(emailMapper.map(emailRequest))));
    }

    @PutMapping("/emails/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update existing email")
    public ResponseEntity<ApiEmailResponse> updateEmail(@PathVariable("id") UUID id, @RequestBody ApiEmailRequest emailRequest) {
        return ResponseEntity.ok(emailMapper.map(emailService.updateEmail(id, emailMapper.map(emailRequest))));
    }

    @DeleteMapping("/emails/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Delete email by id")
    public ResponseEntity<HttpStatus> deleteEmail(@PathVariable("id") UUID id) {
        try {
            emailService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception _) {
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }

    @GetMapping("/emails/count/{emailAddress}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get email count by email address")
    public ResponseEntity<ApiEmailResponse> getEmailCountByEmailAddress(@PathVariable("emailAddress") String emailAddress) {
        Email countByEmailAddress = emailService.getEmailCountByEmailAddress(emailAddress);
        if (Objects.nonNull(countByEmailAddress))
            return ResponseEntity.ok(emailMapper.map(countByEmailAddress));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
