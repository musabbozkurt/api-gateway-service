package com.mb.modulith.commerce.messaging.queue.notifications.producer;

import com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationType;
import com.mb.modulith.commerce.messaging.queue.notifications.events.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationProducer {

    void sendNotification(NotificationType notificationType, List<Param> params, String language, UUID tenantId);
}
