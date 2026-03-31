package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationImpl implements NotificationStrategy {

    private final SseNotificationService sseSender;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        String id = UUID.randomUUID().toString();

        try {
            log.info("Sending push notification. Id: {}, UserId: {}, Title: {}", id, request.getUserId(), request.getTitle());

            NotificationEventDto dto = new NotificationEventDto();
            dto.setUserId(request.getUserId());
            dto.setTitle(request.getTitle());
            dto.setBody(request.getBody());
            dto.setChannel(NotificationChannel.PUSH);
            dto.setData(request.getData());

            sseSender.send(dto);

            log.info("Push notification sent successfully. Id: {}", id);

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.PUSH)
                    .success(true)
                    .message("Push notification sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred while sending push notification. Id: {}, UserId: {}, Exception: {}", id, request.getUserId(), ExceptionUtils.getStackTrace(e));

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.PUSH)
                    .success(false)
                    .message("Failed to send push notification: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }
}
