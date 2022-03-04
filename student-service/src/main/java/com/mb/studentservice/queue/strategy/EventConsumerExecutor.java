package com.mb.studentservice.queue.strategy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumerExecutor {

    private final List<EventConsumerStrategy> strategies;

    public void execute(Message<?> message) {
        try {
            String eventType = (String) message.getHeaders().get("eventType");

            Optional<EventConsumerStrategy> opt = strategies.stream()
                    .filter(strategy -> strategy.canExecute(eventType))
                    .findFirst();

            if (opt.isPresent()) {
                log.info("Found consumer strategy class:{}", opt.get().getClass());
                opt.get().execute(message);
            } else {
                log.info("Strategy class could not be found while consuming message. eventType:{}", eventType);
            }
        } catch (Exception e) {
            log.info("Error occurred while executing ConsumerEventExecutor Exception: {} Message: {}", ExceptionUtils.getStackTrace(e), message);
        }
    }
}
