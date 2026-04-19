package com.mb.notificationservice.api.controller;

import com.mb.notificationservice.api.controller.swagger.NotificationTemplateApi;
import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationTemplateService;
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
public class NotificationTemplateController implements NotificationTemplateApi {

    private final NotificationTemplateService notificationTemplateService;

    @Override
    @PostMapping
    public ApiResponse<NotificationTemplateResponse> create(@RequestBody NotificationTemplateRequest request) {
        return new ApiResponse<>(notificationTemplateService.create(request));
    }

    @Override
    @PutMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> update(@PathVariable Long id,
                                                            @RequestBody NotificationTemplateRequest request) {
        return new ApiResponse<>(notificationTemplateService.update(id, request));
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<NotificationTemplateResponse> getById(@PathVariable Long id) {
        return new ApiResponse<>(notificationTemplateService.getById(id));
    }

    @Override
    @GetMapping("/code/{code}/channel/{channel}")
    public ApiResponse<NotificationTemplateResponse> getByCodeAndChannel(@PathVariable String code,
                                                                         @PathVariable NotificationChannel channel) {
        return new ApiResponse<>(notificationTemplateService.getByCodeAndChannel(code, channel));
    }

    @Override
    @GetMapping
    public ApiResponse<Page<NotificationTemplateResponse>> getAll(Pageable pageable) {
        return new ApiResponse<>(notificationTemplateService.getAll(pageable));
    }

    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        notificationTemplateService.delete(id);
        return ApiResponse.ok(null);
    }
}
