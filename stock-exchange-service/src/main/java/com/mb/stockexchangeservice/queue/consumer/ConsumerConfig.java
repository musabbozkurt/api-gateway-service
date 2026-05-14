package com.mb.stockexchangeservice.queue.consumer;

import com.mb.stockexchangeservice.queue.event.InternalEvent;
import com.mb.stockexchangeservice.queue.event.UserCreatedEvent;
import com.mb.stockexchangeservice.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConsumerConfig {

    private final ConsumerStrategyExecutor consumerStrategyExecutor;

    @Bean
    public Function<Flux<Message<InternalEvent>>, Flux<Message<InternalEvent>>> internalEventProducer() {
        return internalEvent -> internalEvent
                .map(internalEventMessage -> {
                    InternalEvent internalEventMessagePayload = internalEventMessage.getPayload();
                    log.info("Received an internal event. internalEventProducer - event: {}, eventType: {}, eventTypeHeader: {}",
                            internalEventMessagePayload,
                            internalEventMessagePayload.getEventType(),
                            internalEventMessage.getHeaders().get(Constants.EVENT_TYPE_HEADER_KEY));
                    return internalEventMessage;
                });
    }

    @Bean
    public Consumer<Message<InternalEvent>> internalEventConsumer() {
        return message -> consumerStrategyExecutor.dispatchEvent(message.getPayload(), message.getHeaders());
    }

    @Bean
    public Consumer<Message<UserCreatedEvent>> userCreatedEvent() {
        return message -> consumerStrategyExecutor.dispatchEvent(message.getPayload(), message.getHeaders());
    }
}
