package com.mb.modulith.commerce.messaging.queue.notifications.producer.impl;

import com.mb.modulith.commerce.messaging.queue.QueueChannels;
import com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationEvent;
import com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationType;
import com.mb.modulith.commerce.messaging.queue.notifications.events.Param;
import com.mb.modulith.commerce.messaging.queue.notifications.events.TransferType;
import com.mb.modulith.commerce.messaging.queue.notifications.producer.NotificationProducer;
import com.mb.modulith.commerce.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducerImpl implements NotificationProducer {

    private final StreamBridge streamBridge;

    @Override
    public void sendNotification(NotificationType notificationType, List<Param> params, String language, UUID tenantId) {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .transferType(TransferType.NOTIFICATION)
                .notificationType(notificationType)
                .language(language)
                .params(params)
                .build();

        send(notificationEvent, tenantId != null ? tenantId : UUID.randomUUID());
    }

    private void send(NotificationEvent notificationEvent, UUID tenantId) {
        Message<NotificationEvent> message = MessageBuilder.withPayload(notificationEvent)
                .setHeader("tenant_id", tenantId)
                .setHeader("eventType", notificationEvent.getNotificationType())
                .build();
        TransactionUtils.onCommit(() -> {
            log.info("Sent notification to process {}.", notificationEvent);
            streamBridge.send(QueueChannels.NOTIFICATION_PRODUCER, message);
        });
    }
}
