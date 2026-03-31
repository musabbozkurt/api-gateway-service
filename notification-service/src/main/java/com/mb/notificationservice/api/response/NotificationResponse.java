package com.mb.notificationservice.api.response;

import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponse {

    @Schema(example = "a3b1c2d4-e5f6-7890-abcd-ef1234567890")
    private String id;

    @Schema(example = "EMAIL")
    private NotificationChannel channel;

    @Schema(example = "true")
    private boolean success;

    @Schema(example = "Notification queued successfully")
    private String message;
}
