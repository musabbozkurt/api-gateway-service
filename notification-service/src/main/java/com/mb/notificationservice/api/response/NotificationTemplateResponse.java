package com.mb.notificationservice.api.response;

import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationTemplateResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "EMAIL")
    private NotificationChannel channel;

    @Schema(example = "ORDER_CONFIRMATION")
    private String code;

    @Schema(example = "Order Confirmation Template")
    private String name;

    @Schema(example = "Your Order Has Been Confirmed")
    private String subject;

    @Schema(example = "<p>Dear {{customerName}}, your order #{{orderNumber}} has been confirmed.</p>")
    private String body;

    @Schema(example = "Email template sent after a successful order placement")
    private String description;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "System")
    private String createdBy;

    @Schema(example = "2026-03-28T10:15:30")
    private LocalDateTime createdDate;

    @Schema(example = "System")
    private String lastModifiedBy;

    @Schema(example = "2026-03-28T12:30:00")
    private LocalDateTime lastModifiedDate;
}
