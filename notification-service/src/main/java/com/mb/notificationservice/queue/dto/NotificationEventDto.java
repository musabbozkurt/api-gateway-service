package com.mb.notificationservice.queue.dto;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
public class NotificationEventDto {

    private UUID id;
    private NotificationChannel channel;
    private NotificationLevel level = NotificationLevel.INFO;
    private String subject;
    private String body;
    private String title;
    private String templateCode;
    private Map<String, Object> templateParameters = new HashMap<>();
    private Map<String, String> data = new HashMap<>();
    private Long userId;
    private Set<String> recipients = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    private Long createdBy;

    public NotificationEventDto() {
        this.id = UUID.randomUUID();
    }
}
