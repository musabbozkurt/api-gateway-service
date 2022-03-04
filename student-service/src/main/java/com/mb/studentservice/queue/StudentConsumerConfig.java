package com.mb.studentservice.queue;

import com.mb.studentservice.queue.strategy.EventConsumerExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudentConsumerConfig {

    private final EventConsumerExecutor eventConsumerExecutor;

    @Bean
    public Consumer<Message<?>> swagger2Consumer() {
        return message -> {
            log.info("Received a swagger2 consumer event. swagger2Consumer - Payload: {} headers: {}.", message.getPayload(), message.getHeaders());
            executeStrategy(message);
        };
    }

    private void executeStrategy(Message<?> message) {
        try {
            eventConsumerExecutor.execute(message);
        } catch (Exception e) {
            log.info("Failed to process consumer ex: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
