package com.mb.notificationservice.api.controller.swagger;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Notification Template", description = "CRUD operations for notification templates")
public interface NotificationTemplateApi {

    @Operation(
            summary = "Create a notification template",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(name = "Email template", value = """
                                    {
                                      "channel": "EMAIL",
                                      "code": "ORDER_CONFIRMATION",
                                      "name": "Order Confirmation",
                                      "subject": "Your Order #{{orderNumber}} is Confirmed",
                                      "body": "<p>Dear {{customerName}}, your order #{{orderNumber}} has been confirmed.</p>",
                                      "description": "Sent after a successful order placement",
                                      "active": true
                                    }"""),
                            @ExampleObject(name = "SMS template", value = """
                                    {
                                      "channel": "SMS",
                                      "code": "OTP_VERIFICATION",
                                      "name": "OTP Verification",
                                      "body": "Your verification code is {{otpCode}}. Valid for {{validMinutes}} minutes.",
                                      "description": "OTP code sent for user verification",
                                      "active": true
                                    }"""),
                            @ExampleObject(name = "Push template", value = """
                                    {
                                      "channel": "PUSH",
                                      "code": "ORDER_SHIPPED",
                                      "name": "Order Shipped",
                                      "subject": "Your order is on its way!",
                                      "body": "Your order #{{orderNumber}} has been shipped and will arrive by {{deliveryDate}}.",
                                      "description": "Push notification sent when an order is shipped",
                                      "active": true
                                    }""")
                    })
            )
    )
    ApiResponse<NotificationTemplateResponse> create(@Valid NotificationTemplateRequest request);

    @Operation(
            summary = "Update a notification template",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(name = "Email template", value = """
                                    {
                                      "channel": "EMAIL",
                                      "code": "ORDER_CONFIRMATION",
                                      "name": "Order Confirmation v2",
                                      "subject": "Order #{{orderNumber}} Confirmed",
                                      "body": "<h1>Thank you!</h1><p>Dear {{customerName}}, your order has been confirmed.</p>",
                                      "description": "Updated order confirmation email template",
                                      "active": true
                                    }"""),
                            @ExampleObject(name = "SMS template", value = """
                                    {
                                      "channel": "SMS",
                                      "code": "OTP_VERIFICATION",
                                      "name": "OTP Verification v2",
                                      "body": "{{otpCode}} is your verification code. Expires in {{validMinutes}} min.",
                                      "description": "Updated OTP verification SMS",
                                      "active": true
                                    }"""),
                            @ExampleObject(name = "Push template", value = """
                                    {
                                      "channel": "PUSH",
                                      "code": "ORDER_SHIPPED",
                                      "name": "Order Shipped v2",
                                      "subject": "Shipment update",
                                      "body": "Great news! Order #{{orderNumber}} is out for delivery.",
                                      "description": "Updated shipment push notification",
                                      "active": true
                                    }""")
                    })
            )
    )
    ApiResponse<NotificationTemplateResponse> update(@Parameter(description = "Template ID", example = "1") Long id,
                                                     @Valid NotificationTemplateRequest request);

    @Operation(summary = "Get a notification template by ID")
    ApiResponse<NotificationTemplateResponse> getById(@Parameter(description = "Template ID", example = "1") Long id);

    @Operation(summary = "Get a notification template by code and channel")
    ApiResponse<NotificationTemplateResponse> getByCodeAndChannel(@Parameter(description = "Template code", example = "ORDER_CONFIRMATION") String code,
                                                                  @Parameter(description = "Notification channel", example = "EMAIL") NotificationChannel channel);

    @Operation(summary = "Get all notification templates (paginated)")
    ApiResponse<Page<NotificationTemplateResponse>> getAll(Pageable pageable);

    @Operation(summary = "Delete a notification template")
    ApiResponse<Void> delete(@Parameter(description = "Template ID", example = "1") Long id);
}
