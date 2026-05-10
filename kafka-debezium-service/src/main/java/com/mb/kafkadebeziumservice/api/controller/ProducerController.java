package com.mb.kafkadebeziumservice.api.controller;

import com.mb.kafkadebeziumservice.queue.producer.ProducerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/producer")
public class ProducerController {

    private final ProducerService producerService;

    @GetMapping
    @Operation(summary = "Check health of producer")
    public ResponseEntity<String> health() {
        log.info("Producer health is called.");
        return ResponseEntity.ok("Producer is running.");
    }

    @PostMapping("/{topic}")
    @Operation(summary = "Publish messages. {topic} should be obtained from application.yml")
    public ResponseEntity<String> publishToTopic(@PathVariable("topic") final String topic, @RequestBody String message) {
        producerService.publishMessage(topic, message);
        return ResponseEntity.ok("Message is published.");
    }
}
