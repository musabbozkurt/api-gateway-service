package com.mb.notificationservice.api.controller;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
@Tag(name = "Notification Template", description = "CRUD operations for notification templates")
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @PostMapping
    @Operation(summary = "Create a notification template")
    public ApiResponse<NotificationTemplateResponse> create(@Valid @RequestBody NotificationTemplateRequest request) {
        return new ApiResponse<>(notificationTemplateService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a notification template")
    public ApiResponse<NotificationTemplateResponse> update(@Parameter(description = "Template ID", example = "1") @PathVariable Long id,
                                                            @Valid @RequestBody NotificationTemplateRequest request) {
        return new ApiResponse<>(notificationTemplateService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification template by ID")
    public ApiResponse<NotificationTemplateResponse> getById(@Parameter(description = "Template ID", example = "1") @PathVariable Long id) {
        return new ApiResponse<>(notificationTemplateService.getById(id));
    }

    @GetMapping("/code/{code}/channel/{channel}")
    @Operation(summary = "Get a notification template by code and channel")
    public ApiResponse<NotificationTemplateResponse> getByCodeAndChannel(@Parameter(description = "Template code", example = "ORDER_CONFIRMATION") @PathVariable String code,
                                                                         @Parameter(description = "Notification channel", example = "EMAIL") @PathVariable NotificationChannel channel) {
        return new ApiResponse<>(notificationTemplateService.getByCodeAndChannel(code, channel));
    }

    @GetMapping
    @Operation(summary = "Get all notification templates (paginated)")
    public ApiResponse<Page<NotificationTemplateResponse>> getAll(Pageable pageable) {
        return new ApiResponse<>(notificationTemplateService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification template")
    public ApiResponse<Void> delete(@Parameter(description = "Template ID", example = "1") @PathVariable Long id) {
        notificationTemplateService.delete(id);
        return ApiResponse.ok(null);
    }
}
