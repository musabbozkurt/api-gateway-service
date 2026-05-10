package com.mb.kafkadebeziumservice.api.controller;

import com.mb.kafkadebeziumservice.queue.consumer.ConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/consumer")
public class ConsumerController {

    private final ConsumerService consumerService;

    @GetMapping
    @Operation(summary = "Check health of consumer")
    public ResponseEntity<String> health() {
        log.info("Consumer health is called.");
        return ResponseEntity.ok("Consumer service is up.");
    }

    @GetMapping("/orders")
    @Operation(summary = "Retrieve List of unprocessed messages from orders topic")
    public ResponseEntity<List<String>> consumeOrders() {
        return ResponseEntity.ok(consumerService.consumeOrders());
    }
}
