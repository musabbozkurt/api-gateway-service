package com.mb.notificationservice.service;

import com.mb.notificationservice.queue.dto.NotificationEventDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseNotificationService {

    SseEmitter register(Long userId);

    void send(NotificationEventDto notification);
}
