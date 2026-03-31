package com.mb.notificationservice.api.request;

import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationTemplateRequest {

    @Schema(example = "EMAIL")
    @NotNull(message = "{validation.template.channel.notNull}")
    private NotificationChannel channel;

    @Schema(example = "ORDER_CONFIRMATION")
    @NotBlank(message = "{validation.template.code.notBlank}")
    @Size(max = 255, message = "{validation.template.code.size}")
    private String code;

    @Schema(example = "Order Confirmation Template")
    @Size(max = 255, message = "{validation.template.name.size}")
    private String name;

    @Schema(example = "Your Order Has Been Confirmed")
    @Size(max = 255, message = "{validation.template.subject.size}")
    private String subject;

    @NotBlank(message = "{validation.template.body.notBlank}")
    @Schema(example = "<p>Dear {{customerName}}, your order #{{orderNumber}} has been confirmed.</p>")
    private String body;

    @Size(max = 500, message = "{validation.template.description.size}")
    @Schema(example = "Email template sent after a successful order placement")
    private String description;

    @Schema(example = "true")
    private boolean active = true;
}
