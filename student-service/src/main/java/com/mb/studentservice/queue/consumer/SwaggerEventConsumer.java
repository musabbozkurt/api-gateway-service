package com.mb.studentservice.queue.consumer;

import com.mb.studentservice.enums.EventType;
import com.mb.studentservice.queue.strategy.EventConsumerStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class SwaggerEventConsumer implements EventConsumerStrategy {

    @Override
    @Transactional(noRollbackFor = RuntimeException.class) // If the system is transactional then it can be used.
    public void execute(Message<?> message) {
        try {
            log.info("Message is consumed in SwaggerEventConsumer. Message : {}", message);
        } catch (Exception e) {
            log.error("Exception occurred while executing SwaggerEventConsumer. Exception : {}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public boolean canExecute(String eventType) {
        return EventType.SWAGGER_EVENT.name().equals(eventType);
    }
}
