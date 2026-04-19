package com.mb.notificationservice.service.impl;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.client.firebase.service.FcmService;
import com.mb.notificationservice.data.entity.DeviceToken;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.DeviceTokenService;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationImpl implements NotificationStrategy {

    private final SseNotificationService sseNotificationService;
    private final FcmService fcmService;
    private final DeviceTokenService deviceTokenService;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            Set<String> applications = request.getApplications();

            if (CollectionUtils.isEmpty(applications)) {
                log.warn("No applications specified for push notification. UserId: {}", request.getUserId());
                return NotificationResponse.builder()
                        .channel(NotificationChannel.PUSH)
                        .success(false)
                        .message("No applications specified for push notification")
                        .build();
            }

            log.info("Sending push notification. UserId: {}, Title: {}, Applications: {}", request.getUserId(), request.getTitle(), applications);

            NotificationEventDto dto = new NotificationEventDto();
            dto.setUserId(request.getUserId());
            dto.setTitle(request.getTitle());
            dto.setBody(request.getBody());
            dto.setChannel(NotificationChannel.PUSH);
            dto.setApplications(applications);
            dto.setData(request.getData());

            sseNotificationService.send(dto);

            List<DeviceToken> activeTokens = deviceTokenService.getActiveTokensByUserIdAndApplications(request.getUserId(), applications);

            if (activeTokens.isEmpty()) {
                log.warn("Server-Sent Events is sent but no active Firebase device token found for any application. UserId: {}, Applications: {}", request.getUserId(), applications);
                return NotificationResponse.builder()
                        .channel(NotificationChannel.PUSH)
                        .success(false)
                        .message("Server-Sent Events is sent but no active Firebase device token found for any application")
                        .build();
            }

            for (DeviceToken deviceToken : activeTokens) {
                Message message = Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder().setTitle(request.getTitle()).setBody(request.getBody()).build())
                        .build();

                fcmService.send(message, deviceToken.getApplication());
                log.info("Firebase push notification sent successfully. UserId: {}, Application: {}", request.getUserId(), deviceToken.getApplication());
            }

            return NotificationResponse.builder()
                    .channel(NotificationChannel.PUSH)
                    .success(true)
                    .message("Server-Sent Events and Firebase push notification sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred while sending push notification. UserId: {}, Applications: {}, Exception: {}", request.getUserId(), request.getApplications(), ExceptionUtils.getStackTrace(e));

            return NotificationResponse.builder()
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
