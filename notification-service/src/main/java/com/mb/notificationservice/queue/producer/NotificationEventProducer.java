package com.mb.notificationservice.queue.producer;

import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.util.ServiceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, NotificationEventDto> kafkaTemplate;

    public void produce(NotificationEventDto notificationEventDto) {
        kafkaTemplate.send(ServiceConstants.NOTIFICATION_TOPIC, notificationEventDto);
        log.info("Notification event produced. Id: {}, Channel: {}", notificationEventDto.getId(), notificationEventDto.getChannel());
    }
}
