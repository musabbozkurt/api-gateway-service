package com.mb.modulith.commerce.messaging.queue;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class ConsumerEventExecutor {
    private final List<ConsumerEventStrategy> strategies;

    public void execute(Message<?> event) {
        String userEventType = (String) event.getHeaders().get("eventType");
        Optional<ConsumerEventStrategy> opt = strategies.stream()
                .filter(strategy -> strategy.canExecute(userEventType))
                .findFirst();

        if (opt.isPresent()) {
            log.info("Found consumer strategy class:{}", opt.get().getClass());
            opt.get().execute(event);
        } else {
            log.info("Not found while consume event. eventType:{}", userEventType);
        }
    }
}
