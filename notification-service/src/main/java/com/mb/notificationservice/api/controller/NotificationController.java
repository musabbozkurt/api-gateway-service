package com.mb.notificationservice.api.controller;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationService;
import com.mb.notificationservice.service.SseNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Send notifications via Email, SMS or Push channels")
public class NotificationController {

    private final SseNotificationService sseNotificationService;
    private final NotificationService notificationService;

    @Operation(summary = "Subscribe to real-time notifications via SSE")
    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Parameter(description = "User ID to subscribe", example = "12345") @PathVariable Long userId) {
        return sseNotificationService.register(userId);
    }

    @PostMapping("/send")
    @Operation(
            summary = "Send a notification asynchronously",
            description = "Queues a notification for async delivery via the specified channel",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(name = "Email", value = """
                                    {
                                      "channel": "EMAIL",
                                      "level": "INFO",
                                      "recipients": ["user@example.com"],
                                      "subject": "Order Confirmation",
                                      "body": "<p>Your order #1234 has been confirmed.</p>",
                                      "cc": ["manager@example.com"],
                                      "bcc": ["archive@example.com"]
                                    }"""),
                            @ExampleObject(name = "Email with Template", value = """
                                    {
                                      "channel": "EMAIL",
                                      "level": "INFO",
                                      "recipients": ["user@example.com"],
                                      "templateCode": "ORDER_CONFIRMATION",
                                      "templateParameters": {
                                        "customerName": "John Doe",
                                        "orderNumber": "1234",
                                        "orderDate": "2026-03-31",
                                        "totalAmount": "$49.99"
                                      },
                                      "cc": ["manager@example.com"]
                                    }"""),
                            @ExampleObject(name = "SMS", value = """
                                    {
                                      "channel": "SMS",
                                      "level": "INFO",
                                      "recipients": ["905321234567"],
                                      "body": "Your verification code is 123456"
                                    }"""),
                            @ExampleObject(name = "Push", value = """
                                    {
                                      "channel": "PUSH",
                                      "level": "INFO",
                                      "userId": 12345,
                                      "title": "New Order",
                                      "body": "You have a new order to review",
                                      "data": {
                                        "orderId": "1234",
                                        "action": "OPEN_ORDER"
                                      }
                                    }""")
                    })
            )
    )
    public ApiResponse<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return new ApiResponse<>(notificationService.sendAsync(request));
    }

    @PostMapping("/send/batch")
    @Operation(summary = "Send multiple notifications asynchronously")
    public ApiResponse<List<NotificationResponse>> sendBatch(@Valid @RequestBody List<NotificationRequest> requests) {
        return new ApiResponse<>(notificationService.sendAsyncMultiple(requests));
    }

    @PostMapping("/send/sync")
    @Operation(summary = "Send a notification synchronously")
    public ApiResponse<NotificationResponse> sendSync(@Valid @RequestBody NotificationRequest request) {
        return new ApiResponse<>(notificationService.sendSync(request));
    }

    @GetMapping
    @Operation(summary = "Get notifications for the current user (paginated)")
    public ApiResponse<Page<NotificationSummaryResponse>> getNotifications(@Parameter(description = "Filter by notification channel", example = "PUSH")
                                                                           @RequestParam(required = false) NotificationChannel channel,
                                                                           @ParameterObject Pageable pageable) {
        return new ApiResponse<>(notificationService.getNotifications(pageable, channel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification detail by ID")
    public ApiResponse<NotificationDetailResponse> getNotificationDetailById(@Parameter(description = "Notification ID", example = "1") @PathVariable Long id) {
        return new ApiResponse<>(notificationService.getNotificationDetailById(id));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ApiResponse<Long> getUnreadCount() {
        return new ApiResponse<>(notificationService.getUnreadCount());
    }
}
