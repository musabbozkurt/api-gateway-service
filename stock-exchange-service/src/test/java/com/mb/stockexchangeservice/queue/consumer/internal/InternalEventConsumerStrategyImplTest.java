package com.mb.stockexchangeservice.queue.consumer.internal;

import com.mb.stockexchangeservice.queue.event.Event;
import com.mb.stockexchangeservice.queue.event.InternalEvent;
import com.mb.stockexchangeservice.queue.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InternalEventConsumerStrategyImplTest {

    private InternalEventConsumerStrategyImpl strategy;

    @BeforeEach
    void setUp() {
        strategy = new InternalEventConsumerStrategyImpl();
    }

    @Test
    void execute_ShouldLogEvent_WhenInternalEventReceived() {
        InternalEvent event = InternalEvent.builder().randomId(UUID.randomUUID()).build();
        assertDoesNotThrow(() -> strategy.execute(event));
    }

    @Test
    void canExecute_ShouldReturnTrue_WhenEventTypeIsInternalEvent() {
        InternalEvent event = InternalEvent.builder().randomId(UUID.randomUUID()).build();
        assertThat(strategy.canExecute(event)).isTrue();
    }

    @Test
    void canExecute_ShouldReturnFalse_WhenEventTypeIsNotInternalEvent() {
        UserCreatedEvent event = UserCreatedEvent.builder().build();
        assertThat(strategy.canExecute(event)).isFalse();
    }

    @Test
    void canExecute_ShouldReturnFalse_WhenEventTypeIsNull() {
        Event event = () -> null;
        assertThat(strategy.canExecute(event)).isFalse();
    }
}
