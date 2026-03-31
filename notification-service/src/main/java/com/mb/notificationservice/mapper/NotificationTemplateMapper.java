package com.mb.notificationservice.mapper;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateMapper {

    public NotificationTemplate convert(NotificationTemplateRequest request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setChannel(request.getChannel());
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());
        template.setActive(request.isActive());
        return template;
    }

    public void update(NotificationTemplate template, NotificationTemplateRequest request) {
        template.setChannel(request.getChannel());
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());
        template.setActive(request.isActive());
    }

    public NotificationTemplateResponse convert(NotificationTemplate template) {
        NotificationTemplateResponse response = new NotificationTemplateResponse();
        response.setId(template.getId());
        response.setChannel(template.getChannel());
        response.setCode(template.getCode());
        response.setName(template.getName());
        response.setSubject(template.getSubject());
        response.setBody(template.getBody());
        response.setDescription(template.getDescription());
        response.setActive(template.isActive());
        response.setCreatedBy(template.getCreatedBy());
        response.setCreatedDate(template.getCreatedDate());
        response.setLastModifiedBy(template.getLastModifiedBy());
        response.setLastModifiedDate(template.getLastModifiedDate());
        return response;
    }
}
