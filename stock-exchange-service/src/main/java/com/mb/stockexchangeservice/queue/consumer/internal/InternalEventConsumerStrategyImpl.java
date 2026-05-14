package com.mb.stockexchangeservice.queue.consumer.internal;

import com.mb.stockexchangeservice.queue.consumer.ConsumerStrategy;
import com.mb.stockexchangeservice.queue.event.Event;
import com.mb.stockexchangeservice.queue.event.StockExchangeServiceEventType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class InternalEventConsumerStrategyImpl implements ConsumerStrategy {

    @Override
    public void execute(Event event) {
        log.info("Received an internal event. execute - event: {}", event);
    }

    @Override
    public boolean canExecute(Event event) {
        return StockExchangeServiceEventType.INTERNAL_EVENT.equals(event.getEventType());
    }
}
