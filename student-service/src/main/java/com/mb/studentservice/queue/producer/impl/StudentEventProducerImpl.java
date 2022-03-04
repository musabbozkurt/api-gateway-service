package com.mb.studentservice.queue.producer.impl;

import com.mb.studentservice.enums.EventType;
import com.mb.studentservice.queue.producer.StudentEventProducer;
import com.mb.studentservice.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentEventProducerImpl implements StudentEventProducer {

    private final StreamBridge streamBridge;

    @Override
    public void publishEvent(String message, EventType eventType) {
        Message<?> buildMessage = MessageBuilder
                .withPayload(message)
                .setHeader("eventType", eventType)
                .build();

        log.info("Publishing student event. StudentEventProducerImpl Message: {}.", buildMessage);
        streamBridge.send(Constants.STUDENT_PRODUCER, buildMessage);
    }

}