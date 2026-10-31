package com.mb.modulith.commerce.messaging.queue;

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
public class ConsumerConfig {

    private final ConsumerEventExecutor consumerEventExecutor;

    /// Bean name -> notificationConsumer should be as same as the definition name in application.yml -> definition: notificationConsumer
    @Bean
    public Consumer<Message<?>> notificationConsumer() {
        return message -> {
            log.info("Received a transaction consumer event. transactionConsumer - Payload: {} headers: {}.", message.getPayload(), message.getHeaders());
            executeStrategy(message);
        };
    }

    private void executeStrategy(Message<?> message) {
        try {
            consumerEventExecutor.execute(message);
        } catch (Exception e) {
            log.info("Failed to process consumer ex: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
