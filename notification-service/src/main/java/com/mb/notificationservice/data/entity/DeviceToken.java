package com.mb.notificationservice.data.entity;

import com.mb.notificationservice.enums.DevicePlatform;
import com.mb.notificationservice.util.ServiceConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "device_token",
        schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "application"})
        }
)
public class DeviceToken extends BaseEntity {

    @Id
    @SequenceGenerator(
            schema = ServiceConstants.NOTIFICATION_SCHEMA_NAME,
            name = "device_token_seq",
            sequenceName = "device_token_seq",
            allocationSize = 1
    )
    @GeneratedValue(generator = "device_token_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private DevicePlatform platform;

    @Column(name = "application", nullable = false)
    private String application;

    @Column(name = "active")
    private boolean active = true;
}
