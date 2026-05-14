package com.mb.stockexchangeservice.queue.consumer;

import com.mb.stockexchangeservice.queue.event.InternalEvent;
import com.mb.stockexchangeservice.utils.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerStrategyExecutorTest {

    @Mock
    private ConsumerStrategy strategy1;

    @Mock
    private ConsumerStrategy strategy2;

    @Test
    void dispatchEvent_ShouldExecuteMatchingStrategy_WhenStrategyCanExecute() {
        // Arrange
        InternalEvent event = InternalEvent.builder()
                .randomId(UUID.randomUUID())
                .build();
        MessageHeaders headers = new MessageHeaders(Map.of(Constants.EVENT_TYPE_HEADER_KEY, "INTERNAL_EVENT"));

        when(strategy1.canExecute(event)).thenReturn(true);

        ConsumerStrategyExecutor executor = new ConsumerStrategyExecutor(List.of(strategy1, strategy2));

        // Act
        executor.dispatchEvent(event, headers);

        // Assertions
        verify(strategy1).canExecute(event);
        verify(strategy1).execute(event);
        verify(strategy2, never()).canExecute(event);
        verify(strategy2, never()).execute(event);
    }

    @Test
    void dispatchEvent_ShouldExecuteSecondStrategy_WhenFirstStrategyCannotExecute() {
        // Arrange
        InternalEvent event = InternalEvent.builder()
                .randomId(UUID.randomUUID())
                .build();
        MessageHeaders headers = new MessageHeaders(Map.of(Constants.EVENT_TYPE_HEADER_KEY, "INTERNAL_EVENT"));

        when(strategy1.canExecute(event)).thenReturn(false);
        when(strategy2.canExecute(event)).thenReturn(true);

        ConsumerStrategyExecutor executor = new ConsumerStrategyExecutor(List.of(strategy1, strategy2));

        // Act
        executor.dispatchEvent(event, headers);

        // Assertions
        verify(strategy1).canExecute(event);
        verify(strategy1, never()).execute(event);
        verify(strategy2).canExecute(event);
        verify(strategy2).execute(event);
    }

    @Test
    void dispatchEvent_ShouldLogWarning_WhenNoStrategyCanExecute() {
        // Arrange
        InternalEvent event = InternalEvent.builder()
                .randomId(UUID.randomUUID())
                .build();
        MessageHeaders headers = new MessageHeaders(Map.of(Constants.EVENT_TYPE_HEADER_KEY, "UNKNOWN_EVENT"));

        when(strategy1.canExecute(event)).thenReturn(false);
        when(strategy2.canExecute(event)).thenReturn(false);

        ConsumerStrategyExecutor executor = new ConsumerStrategyExecutor(List.of(strategy1, strategy2));

        // Act
        executor.dispatchEvent(event, headers);

        // Assertions
        verify(strategy1).canExecute(event);
        verify(strategy1, never()).execute(event);
        verify(strategy2).canExecute(event);
        verify(strategy2, never()).execute(event);
    }

    @Test
    void dispatchEvent_ShouldLogWarning_WhenNoStrategiesExist() {
        // Arrange
        InternalEvent event = InternalEvent.builder()
                .randomId(UUID.randomUUID())
                .build();
        MessageHeaders headers = new MessageHeaders(Map.of(Constants.EVENT_TYPE_HEADER_KEY, "INTERNAL_EVENT"));

        ConsumerStrategyExecutor executor = new ConsumerStrategyExecutor(List.of());

        // Act
        executor.dispatchEvent(event, headers);

        // Assertions - no exception should be thrown
        verify(strategy1, never()).canExecute(event);
        verify(strategy2, never()).canExecute(event);
        assertDoesNotThrow(() -> strategy1.execute(event));
        assertDoesNotThrow(() -> strategy2.execute(event));
    }
}
