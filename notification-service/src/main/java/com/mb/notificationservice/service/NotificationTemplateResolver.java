package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationRequest;

public interface NotificationTemplateResolver {

    void resolve(NotificationRequest request);
}
