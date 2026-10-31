package com.mb.modulith.email.api.controller;

import com.google.gson.Gson;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.mapping.EmailMapper;
import com.mb.modulith.email.service.EmailResourceService;
import com.mb.modulith.email.service.EmailService;
import com.mb.modulith.email.service.FileIOService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/client")
@SuppressWarnings("removal")
public class EmailResourceController {

    private final EmailResourceService emailResourceService;
    private final EmailMapper emailMapper;
    private final JobOperator jobOperator;
    private final Job job;
    private final Gson gson;
    private final FileIOService fileIOService;
    private final EmailService emailService;

    public EmailResourceController(EmailResourceService emailResourceService,
                                   EmailMapper emailMapper,
                                   JobOperator jobOperator,
                                   @Qualifier("exampleJob") Job job,
                                   Gson gson,
                                   FileIOService fileIOService,
                                   EmailService emailService) {
        this.emailResourceService = emailResourceService;
        this.emailMapper = emailMapper;
        this.jobOperator = jobOperator;
        this.job = job;
        this.gson = gson;
        this.fileIOService = fileIOService;
        this.emailService = emailService;
    }

    @GetMapping("/emails/data-set")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get list of Dummy DataSetResponseDto")
    public ResponseEntity<DataSetResponseDto> getDummyDataSetResponseDto() {
        return ResponseEntity.ok(emailMapper.map(emailResourceService.generateDummyDataSetResponseDto()));
    }

    @GetMapping("/emails/data-set-1")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get list of Dummy DataSetResponseDto")
    public ResponseEntity<DataSetResponseDto> getDummyDataSetResponseDto1() {
        return ResponseEntity.ok(emailMapper.map(emailResourceService.generateDummyDataSetResponseDto1()));
    }

    @GetMapping("/emails/data-set-2")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get list of Dummy DataSetResponseDto")
    public ResponseEntity<DataSetResponseDto> getDummyDataSetResponseDto2() {
        return ResponseEntity.ok(emailMapper.map(emailResourceService.generateDummyDataSetResponseDto2()));
    }

    @PostMapping("/emails/data-set")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "check Emails")
    public ResponseEntity<List<ApiEmailResponse>> checkEmails(@RequestBody DataSetResponseDto emailRequest)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            InvalidJobParametersException, JobRestartException {
        String emailRequestJson = gson.toJson(emailRequest);
        fileIOService.writeDataSetResponseDtoToFile(emailRequestJson);
        JobParameters parameters = new JobParametersBuilder()
                .addLong("currentTime", System.currentTimeMillis())
                .toJobParameters();
        jobOperator.run(job, parameters);
        return ResponseEntity.ok(emailMapper.map(emailService.findAll()));
    }
}
