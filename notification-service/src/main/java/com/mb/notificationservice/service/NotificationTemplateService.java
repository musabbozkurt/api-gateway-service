package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationTemplateService {

    NotificationTemplateResponse create(NotificationTemplateRequest request);

    NotificationTemplateResponse update(Long id, NotificationTemplateRequest request);

    NotificationTemplateResponse getById(Long id);

    NotificationTemplateResponse getByCodeAndChannel(String code, NotificationChannel channel);

    Page<NotificationTemplateResponse> getAll(Pageable pageable);

    void delete(Long id);

    NotificationTemplate findActiveByCode(String code, NotificationChannel channel);
}
