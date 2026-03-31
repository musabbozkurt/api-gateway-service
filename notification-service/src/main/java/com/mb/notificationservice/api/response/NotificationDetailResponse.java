package com.mb.notificationservice.api.response;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
public class NotificationDetailResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "PUSH")
    private NotificationChannel channel;

    @Schema(example = "INFO")
    private NotificationLevel level;

    @Schema(example = "TRANSACTIONAL")
    private NotificationType type;

    @Schema(example = "Order Confirmation")
    private String subject;

    @Schema(example = "Your order #1234 has been confirmed.")
    private String body;

    @Schema(example = "New Order")
    private String title;

    @Schema(example = "{\"orderId\": \"1234\", \"action\": \"OPEN_ORDER\"}")
    private Map<String, String> data;

    @Schema(example = "[\"user@example.com\"]")
    private Set<String> recipients;

    @Schema(example = "[\"cc@example.com\"]")
    private Set<String> cc;

    @Schema(example = "[\"bcc@example.com\"]")
    private Set<String> bcc;

    @Schema(example = "SENT")
    private NotificationStatus status;

    @Schema(example = "false")
    private boolean read;

    @Schema(example = "2026-03-30T10:15:30")
    private LocalDateTime readAt;

    @Schema(example = "2026-03-30T10:00:00")
    private LocalDateTime createdAt;
}
