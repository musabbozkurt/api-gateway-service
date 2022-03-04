package com.mb.swagger2.queue.producer.impl;

import com.mb.swagger2.enums.EventType;
import com.mb.swagger2.queue.producer.Swagger2EventProducer;
import com.mb.swagger2.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Swagger2EventProducerImpl implements Swagger2EventProducer {

    private final StreamBridge streamBridge;

    @Override
    public void publishEvent(String message, EventType eventType) {
        Message<?> buildMessage = MessageBuilder
                .withPayload(message)
                .setHeader("eventType", eventType)
                .build();

        log.info("Publishing Swagger2 event. Swagger2EventProducerImpl: {}.", buildMessage);
        streamBridge.send(Constants.SWAGGER2_PRODUCER, buildMessage);
    }

}