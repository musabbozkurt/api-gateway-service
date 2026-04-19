package com.mb.notificationservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ObjectMapper objectMapper;

    public NotificationEventDto convert(NotificationRequest request) {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(request.getChannel());
        dto.setLevel(request.getLevel());
        dto.setApplications(request.getApplications());
        dto.setSubject(request.getSubject());
        dto.setBody(request.getBody());
        dto.setTitle(request.getTitle());
        dto.setTemplateCode(request.getTemplateCode());
        dto.setTemplateParameters(request.getTemplateParameters());
        dto.setData(request.getData());
        dto.setUserId(request.getUserId());
        dto.setRecipients(request.getRecipients());
        dto.setCc(request.getCc());
        dto.setBcc(request.getBcc());
        dto.setCreatedBy(ContextHolder.getContext().userId());
        return dto;
    }

    public Notification convert(NotificationEventDto dto) {
        Notification entity = new Notification();
        entity.setChannel(dto.getChannel());
        entity.setLevel(dto.getLevel());
        entity.setSubject(dto.getSubject());
        entity.setBody(dto.getBody());
        entity.setTitle(dto.getTitle());
        entity.setTemplateCode(dto.getTemplateCode());

        try {
            if (dto.getTemplateParameters() != null && !dto.getTemplateParameters().isEmpty()) {
                entity.setTemplateParameters(objectMapper.writeValueAsString(dto.getTemplateParameters()));
            }
            if (dto.getData() != null && !dto.getData().isEmpty()) {
                entity.setData(objectMapper.writeValueAsString(dto.getData()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing notification data. Exception: {}", ExceptionUtils.getStackTrace(e));
        }

        entity.setUserId(dto.getUserId());
        entity.setApplications(Objects.requireNonNullElse(dto.getApplications(), Set.of()));
        entity.setRecipients(Objects.requireNonNullElse(dto.getRecipients(), Set.of()));
        entity.setCc(Objects.requireNonNullElse(dto.getCc(), Set.of()));
        entity.setBcc(Objects.requireNonNullElse(dto.getBcc(), Set.of()));

        entity.setCreatedBy(String.valueOf(dto.getCreatedBy()));

        return entity;
    }

    public NotificationSummaryResponse toNotificationSummaryResponse(Notification entity) {
        return NotificationSummaryResponse.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .subject(entity.getSubject())
                .title(entity.getTitle())
                .level(entity.getLevel())
                .status(entity.getStatus())
                .read(entity.isRead())
                .createdAt(entity.getCreatedDate())
                .build();
    }

    public NotificationDetailResponse toNotificationDetailResponse(Notification entity) {
        Map<String, String> dataMap = Collections.emptyMap();
        try {
            if (entity.getData() != null && !entity.getData().isEmpty()) {
                dataMap = objectMapper.readValue(entity.getData(), new TypeReference<>() {
                });
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing notification data. Exception: {}", ExceptionUtils.getStackTrace(e));
        }

        return NotificationDetailResponse.builder()
                .id(entity.getId())
                .channel(entity.getChannel())
                .level(entity.getLevel())
                .type(entity.getType())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .title(entity.getTitle())
                .data(dataMap)
                .applications(entity.getApplications())
                .recipients(entity.getRecipients())
                .cc(entity.getCc())
                .bcc(entity.getBcc())
                .status(entity.getStatus())
                .read(entity.isRead())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedDate())
                .build();
    }

    public NotificationRequest toRequest(NotificationEventDto dto) {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(dto.getChannel());
        request.setLevel(dto.getLevel());
        request.setApplications(dto.getApplications());
        request.setSubject(dto.getSubject());
        request.setBody(dto.getBody());
        request.setTitle(dto.getTitle());
        request.setTemplateCode(dto.getTemplateCode());
        request.setTemplateParameters(dto.getTemplateParameters());
        request.setData(dto.getData());
        request.setUserId(dto.getUserId());
        request.setRecipients(dto.getRecipients());
        request.setCc(dto.getCc());
        request.setBcc(dto.getBcc());
        return request;
    }
}
