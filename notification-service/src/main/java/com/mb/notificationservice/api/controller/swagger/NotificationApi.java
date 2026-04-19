package com.mb.notificationservice.api.controller.swagger;

import com.mb.notificationservice.api.request.DeviceTokenRequest;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Notification", description = "Send notifications via Email, SMS or Push channels")
public interface NotificationApi {

    @Operation(summary = "Subscribe to real-time notifications via SSE (legacy)")
    SseEmitter subscribe(@Parameter(description = "User ID to subscribe", example = "12345") Long userId);

    @Operation(summary = "Subscribe to real-time notifications via SSE for a specific application")
    SseEmitter subscribeByApplication(@Parameter(description = "User ID to subscribe", example = "12345") Long userId,
                                      @Parameter(description = "Application to subscribe", example = "my-application") String application);

    @Operation(
            summary = "Send a notification asynchronously",
            description = "Queues a notification for async delivery via the specified channel",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Email",
                                    value = """
                                            {
                                              "channel": "EMAIL",
                                              "level": "INFO",
                                              "userId": 12345,
                                              "recipients": ["user@example.com"],
                                              "subject": "Order Confirmation",
                                              "body": "<p>Your order #1234 has been confirmed.</p>",
                                              "cc": ["manager@example.com"],
                                              "bcc": ["archive@example.com"]
                                            }
                                            """),
                            @ExampleObject(
                                    name = "Email with template",
                                    value = """
                                            {
                                              "channel": "EMAIL",
                                              "level": "INFO",
                                              "userId": 12345,
                                              "recipients": ["user@example.com"],
                                              "templateCode": "ORDER_CONFIRMATION",
                                              "templateParameters": {
                                                "orderNumber": "1234",
                                                "customerName": "John Doe"
                                              }
                                            }
                                            """),
                            @ExampleObject(
                                    name = "SMS",
                                    value = """
                                            {
                                              "channel": "SMS",
                                              "level": "INFO",
                                              "userId": 12345,
                                              "recipients": ["905321234567"],
                                              "body": "Your verification code is 123456"
                                            }
                                            """),
                            @ExampleObject(
                                    name = "Push",
                                    value = """
                                            {
                                              "channel": "PUSH",
                                              "level": "INFO",
                                              "applications": ["my-application"],
                                              "userId": 12345,
                                              "title": "New Order",
                                              "body": "You have a new order to review",
                                              "data": {
                                                "orderId": "1234",
                                                "action": "OPEN_ORDER"
                                              }
                                            }
                                            """)
                    })
            )
    )
    ApiResponse<NotificationResponse> send(@Valid NotificationRequest request);

    @Operation(
            summary = "Send multiple notifications asynchronously",
            description = "Queues a batch of notifications for async delivery",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Batch (mixed channels)",
                                    value = """
                                            [
                                              {
                                                "channel": "EMAIL",
                                                "level": "INFO",
                                                "userId": 12345,
                                                "recipients": ["user@example.com"],
                                                "subject": "Order Shipped",
                                                "body": "<p>Your order has been shipped.</p>"
                                              },
                                              {
                                                "channel": "SMS",
                                                "level": "INFO",
                                                "userId": 12345,
                                                "recipients": ["905321234567"],
                                                "body": "Your order has been shipped."
                                              },
                                              {
                                                "channel": "PUSH",
                                                "level": "INFO",
                                                "applications": ["my-application"],
                                                "userId": 12345,
                                                "title": "Order Shipped",
                                                "body": "Your order is on its way!",
                                                "data": {"orderId": "1234"}
                                              }
                                            ]
                                            """)
                    })
            )
    )
    ApiResponse<List<NotificationResponse>> sendBatch(@Valid List<NotificationRequest> requests);

    @Operation(
            summary = "Send a notification synchronously",
            description = "Sends a notification immediately and waits for the result",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Email",
                                    value = """
                                            {
                                              "channel": "EMAIL",
                                              "level": "INFO",
                                              "userId": 12345,
                                              "recipients": ["user@example.com"],
                                              "subject": "Password Reset",
                                              "body": "<p>Click the link to reset your password.</p>"
                                            }
                                            """),
                            @ExampleObject(
                                    name = "SMS",
                                    value = """
                                            {
                                              "channel": "SMS",
                                              "level": "INFO",
                                              "userId": 12345,
                                              "recipients": ["905321234567"],
                                              "body": "Your OTP code is 654321"
                                            }
                                            """),
                            @ExampleObject(
                                    name = "Push",
                                    value = """
                                            {
                                              "channel": "PUSH",
                                              "level": "INFO",
                                              "applications": ["my-application"],
                                              "userId": 12345,
                                              "title": "Payment Received",
                                              "body": "We received your payment of 150.00 TL",
                                              "data": {
                                                "paymentId": "PAY-9876",
                                                "action": "OPEN_PAYMENT"
                                              }
                                            }
                                            """)
                    })
            )
    )
    ApiResponse<NotificationResponse> sendSync(@Valid NotificationRequest request);

    @Operation(
            summary = "Get notifications for the current user (paginated)",
            description = "Returns a paginated summary list of notifications for the authenticated user. Optionally filter by channel (SMS, EMAIL, PUSH)."
    )
    ApiResponse<Page<NotificationSummaryResponse>> getNotifications(@Parameter(description = "Filter by notification channel", example = "PUSH") NotificationChannel channel,
                                                                    Pageable pageable);

    @Operation(
            summary = "Get notification detail by ID",
            description = "Returns the full detail of a specific notification."
    )
    ApiResponse<NotificationDetailResponse> getNotificationDetailById(@Parameter(description = "Notification ID", example = "1") Long id);

    @Operation(
            summary = "Get unread notification count",
            description = "Returns the count of unread notifications for the authenticated user."
    )
    ApiResponse<Long> getUnreadCount();

    @Operation(
            summary = "Register a device token",
            description = "Registers or updates an FCM device token for push notifications. Each user can have only one active token per application.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Android",
                                    value = """
                                            {
                                              "token": "fcm-device-token-abc123",
                                              "platform": "ANDROID",
                                              "application": "my-application"
                                            }
                                            """),
                            @ExampleObject(
                                    name = "iOS",
                                    value = """
                                            {
                                              "token": "fcm-device-token-xyz789",
                                              "platform": "IOS",
                                              "application": "my-application"
                                            }
                                            """),
                            @ExampleObject(
                                    name = "Web",
                                    value = """
                                            {
                                              "token": "fcm-device-token-web456",
                                              "platform": "WEB",
                                              "application": "my-application"
                                            }
                                            """)
                    })
            )
    )
    ApiResponse<Void> registerDeviceToken(@Valid DeviceTokenRequest request);
}
