package com.mb.notificationservice.api.response;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationSummaryResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "PUSH")
    private NotificationChannel channel;

    @Schema(example = "Order Confirmation")
    private String subject;

    @Schema(example = "New Order")
    private String title;

    @Schema(example = "INFO")
    private NotificationLevel level;

    @Schema(example = "SENT")
    private NotificationStatus status;

    @Schema(example = "false")
    private boolean read;

    @Schema(example = "2026-03-30T10:00:00")
    private LocalDateTime createdAt;
}
