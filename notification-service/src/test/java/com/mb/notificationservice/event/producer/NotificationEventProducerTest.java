package com.mb.notificationservice.event.producer;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.queue.producer.NotificationEventProducer;
import com.mb.notificationservice.util.ServiceConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @InjectMocks
    private NotificationEventProducer producer;

    @Mock
    private KafkaTemplate<String, NotificationEventDto> kafkaTemplate;

    @Test
    void produce_ShouldSendToKafka_WhenEventDtoIsProvided() {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.EMAIL);

        producer.produce(dto);

        verify(kafkaTemplate).send(ServiceConstants.NOTIFICATION_TOPIC, dto);
    }
}
