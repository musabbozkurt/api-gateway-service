package com.mb.stockexchangeservice.integration_tests.queue.produer;

import com.mb.stockexchangeservice.queue.QueueChannels;
import com.mb.stockexchangeservice.queue.event.InternalEvent;
import com.mb.stockexchangeservice.queue.event.UserCreatedEvent;
import com.mb.stockexchangeservice.queue.producer.EventProducer;
import com.mb.stockexchangeservice.queue.producer.impl.EventProducerImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableTestBinder
@ActiveProfiles("test-containers")
class EventProducerIntegrationTests {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishEvent_ShouldPublishInternalEvent_WhenValidEventProvided() {
        // Arrange
        EventProducer eventProducer = new EventProducerImpl(streamBridge);
        UUID expectedRandomId = UUID.randomUUID();
        InternalEvent expectedEvent = InternalEvent.builder()
                .randomId(expectedRandomId)
                .build();

        // Act
        eventProducer.publishEvent(QueueChannels.INTERNAL_EVENT_PRODUCER, expectedEvent);
        Message<byte[]> result = outputDestination.receive(5000, "internal-event-destination");

        // Assertions
        assertThat(result).isNotNull();
        InternalEvent actualEvent = objectMapper.readValue(result.getPayload(), InternalEvent.class);
        assertThat(actualEvent).isNotNull();
        assertThat(actualEvent.getRandomId()).isEqualTo(expectedRandomId);
        assertThat(actualEvent.getId()).isEqualTo(expectedEvent.getId());
        assertThat(actualEvent.getEventType()).isEqualTo(expectedEvent.getEventType());
        assertThat(expectedEvent.getEventType()).isEqualTo(result.getHeaders().get("eventType"));
    }

    @Test
    void publishEvent_ShouldPublishUserCreatedEvent_WhenValidEventProvided() {
        // Arrange
        EventProducer eventProducer = new EventProducerImpl(streamBridge);
        UserCreatedEvent event = UserCreatedEvent.builder()
                .build();

        // Act
        eventProducer.publishEvent(QueueChannels.USER_CREATED_EVENT_PRODUCER, event);
        Message<byte[]> result = outputDestination.receive(5000, "user-created-event-destination");

        // Assertions
        assertThat(result).isNotNull();
        UserCreatedEvent actualEvent = objectMapper.readValue(result.getPayload(), UserCreatedEvent.class);
        assertThat(actualEvent).isNotNull();
        assertThat(actualEvent.getId()).isEqualTo(event.getId());
        assertThat(actualEvent.getEventType()).isEqualTo(event.getEventType());
    }
}
