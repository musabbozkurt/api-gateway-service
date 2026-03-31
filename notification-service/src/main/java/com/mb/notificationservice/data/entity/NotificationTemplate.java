package com.mb.notificationservice.data.entity;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.util.ServiceConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code", "channel"})
        }
)
public class NotificationTemplate extends BaseEntity {

    @Id
    @SequenceGenerator(
            schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME,
            name = "notification_template_seq",
            sequenceName = "notification_template_seq",
            allocationSize = 1
    )
    @GeneratedValue(generator = "notification_template_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String code;

    private String name;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String description;

    private boolean active = true;
}
