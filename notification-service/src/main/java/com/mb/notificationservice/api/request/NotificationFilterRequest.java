package com.mb.notificationservice.api.request;

import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationFilterRequest {

    @Parameter(description = "Filter by notification channel", example = "PUSH", schema = @Schema(implementation = NotificationChannel.class))
    private NotificationChannel channel;

    @Parameter(description = "Filter by read status. If omitted, read status is not filtered", schema = @Schema(allowableValues = {"true", "false"}))
    private Boolean isRead;
}
