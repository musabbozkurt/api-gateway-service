package com.mb.notificationservice.data.entity;

import com.mb.notificationservice.data.converter.StringToSetConverter;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.enums.NotificationType;
import com.mb.notificationservice.util.ServiceConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME)
public class Notification extends BaseEntity {

    @Id
    @SequenceGenerator(
            schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME,
            name = "notification_seq",
            sequenceName = "notification_seq",
            allocationSize = 1
    )
    @GeneratedValue(generator = "notification_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationLevel level = NotificationLevel.INFO;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String title;

    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String templateParameters;

    @Column(columnDefinition = "TEXT")
    private String data;

    private Long userId;

    @Convert(converter = StringToSetConverter.class)
    private Set<String> applications = new HashSet<>();

    @Convert(converter = StringToSetConverter.class)
    private Set<String> recipients = new HashSet<>();

    @Convert(converter = StringToSetConverter.class)
    private Set<String> cc = new HashSet<>();

    @Convert(converter = StringToSetConverter.class)
    private Set<String> bcc = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    private String errorMessage;

    private int retryCount = 0;

    private boolean isRead;

    private LocalDateTime readAt;
}
