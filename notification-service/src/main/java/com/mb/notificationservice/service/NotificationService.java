package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendAsync(NotificationRequest request);

    List<NotificationResponse> sendAsyncMultiple(List<NotificationRequest> requests);

    NotificationResponse sendSync(NotificationRequest request);

    Page<NotificationSummaryResponse> getNotifications(Pageable pageable, NotificationChannel channel);

    NotificationDetailResponse getNotificationDetailById(Long id);

    long getUnreadCount();
}
