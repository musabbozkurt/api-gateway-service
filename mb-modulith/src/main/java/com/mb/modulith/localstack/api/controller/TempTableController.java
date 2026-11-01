package com.mb.modulith.localstack.api.controller;

import com.mb.modulith.localstack.client.third_party_api.ThirdPartyApiClient;
import com.mb.modulith.localstack.client.third_party_api.response.ThirdPartyApiResponse;
import com.mb.modulith.localstack.constants.Constants;
import com.mb.modulith.localstack.scheduler.TempTableScheduleJob;
import com.mb.modulith.localstack.scheduler.TempTableScheduleJob4FluxSinksMany;
import com.mb.modulith.localstack.service.impl.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/temp-tables")
public class TempTableController {

    private static final String SCHEDULE_JOB = "ScheduleJob";
    private final MetricService metricService;
    private final TempTableScheduleJob tempTableScheduleJob;
    private final TempTableScheduleJob4FluxSinksMany tempTableScheduleJob4FluxSinksMany;
    private final ScheduledAnnotationBeanPostProcessor postProcessor;
    private final ThirdPartyApiClient thirdPartyApiClient;

    @GetMapping("/temp-table-schedule-job/start")
    public ResponseEntity<String> startTempTableScheduleJob() {
        log.info("Received a request to start TempTable schedule job. startTempTableScheduleJob");
        postProcessor.postProcessAfterInitialization(tempTableScheduleJob, SCHEDULE_JOB);
        metricService.count(Constants.START_SCHEDULE_JOB);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/temp-table-schedule-job/stop")
    public ResponseEntity<String> stopTempTableScheduleJob() {
        log.info("Received a request to stop TempTable schedule job. stopTempTableScheduleJob");
        postProcessor.postProcessBeforeDestruction(tempTableScheduleJob, SCHEDULE_JOB);
        metricService.count(Constants.STOP_SCHEDULE_JOB);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scheduled-tasks")
    public ResponseEntity<String> getScheduledTasks() {
        log.info("Received a request to get scheduled tasks. getScheduledTasks");
        Set<ScheduledTask> scheduledTasks = postProcessor.getScheduledTasks();
        if (!scheduledTasks.isEmpty()) {
            return ResponseEntity.ok(scheduledTasks.toString());
        } else {
            return ResponseEntity.ok("Currently no scheduler tasks are running");
        }
    }

    @GetMapping("/temp-table-schedule-job/cancel")
    public ResponseEntity<String> cancelTempTableScheduleJob() {
        log.info("Received a request to cancel tempTable schedule job. cancelTempTableScheduleJob");
        tempTableScheduleJob4FluxSinksMany.cancelScheduleTempTableDeletion(new Object());
        metricService.count(Constants.STOP_SCHEDULE_JOB);
        return ResponseEntity.ok("scheduleTempTableDeletion job is cancelled");
    }

    @GetMapping("/third-party-call")
    public ResponseEntity<ThirdPartyApiResponse> getResult() {
        ResponseEntity<ThirdPartyApiResponse> result = thirdPartyApiClient.getResult("43153596");
        log.info("Received a request to get result. result: {}", result);
        return result;
    }
}
