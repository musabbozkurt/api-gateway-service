package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.enums.NotificationChannel;

public interface NotificationStrategy {

    NotificationResponse send(NotificationRequest request);

    NotificationChannel getChannel();
}
