package com.mb.notificationservice.api.request;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class NotificationRequest {

    @Schema(example = "EMAIL")
    @NotNull(message = "{validation.channel.notNull}")
    private NotificationChannel channel;

    @Schema(example = "INFO")
    @NotNull(message = "{validation.level.notNull}")
    private NotificationLevel level;

    @Schema(example = "Order Confirmation")
    private String subject;

    @Schema(example = "<p>Your order has been confirmed.</p>")
    private String body;

    @Schema(example = "Order #1234 Confirmed")
    private String title;

    @Schema(example = "ORDER_CONFIRMATION")
    private String templateCode;

    @Schema(example = "{\"orderNumber\": \"1234\", \"customerName\": \"John Doe\"}")
    private Map<String, Object> templateParameters = new HashMap<>();

    @Schema(example = "{\"priority\": \"high\"}")
    private Map<String, String> data = new HashMap<>();

    @Schema(example = "[\"app-one\", \"app-two\"]")
    private Set<String> applications = new HashSet<>();

    @Schema(example = "12345")
    private Long userId;

    @Schema(example = "[\"user1@example.com\", \"user2@example.com\"]")
    private Set<String> recipients = new HashSet<>();

    @Schema(example = "[\"cc@example.com\"]")
    private Set<String> cc = new HashSet<>();

    @Schema(example = "[\"bcc@example.com\"]")
    private Set<String> bcc = new HashSet<>();

    @Schema(
            description = "Email file attachments (Base64-encoded content). Email channel only; subject to configured count/size limits.",
            example = """
                    [
                      {
                        "filename": "invoice.pdf",
                        "contentType": "application/pdf",
                        "contentBase64": "JVBERi0xLjQK..."
                      },
                      {
                        "filename": "report.xlsx",
                        "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "contentBase64": "UEsDBBQAAAAI..."
                      }
                    ]"""
    )
    private List<AttachmentDto> attachments = new ArrayList<>();
}
