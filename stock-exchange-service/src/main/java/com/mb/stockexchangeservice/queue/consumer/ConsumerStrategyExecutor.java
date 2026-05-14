package com.mb.stockexchangeservice.queue.consumer;

import com.mb.stockexchangeservice.queue.event.Event;
import com.mb.stockexchangeservice.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerStrategyExecutor {

    private final List<ConsumerStrategy> strategies;

    public void dispatchEvent(Event event, MessageHeaders messageHeaders) {
        log.info("Received an event. dispatchEvent - event: {}, eventType: {}, eventTypeHeader: {}", event, event.getEventType(), messageHeaders.get(Constants.EVENT_TYPE_HEADER_KEY));
        for (ConsumerStrategy strategy : strategies) {
            if (strategy.canExecute(event)) {
                strategy.execute(event);
                return;
            }
        }
        log.warn("No suitable strategy found. dispatchEvent - event: {}", event);
    }
}
