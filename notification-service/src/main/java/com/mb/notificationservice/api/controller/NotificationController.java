package com.mb.notificationservice.api.controller;

import com.mb.notificationservice.api.controller.swagger.NotificationApi;
import com.mb.notificationservice.api.request.DeviceTokenRequest;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.ApiResponse;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.DeviceTokenService;
import com.mb.notificationservice.service.NotificationService;
import com.mb.notificationservice.service.SseNotificationService;
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
public class NotificationController implements NotificationApi {

    private final SseNotificationService sseNotificationService;
    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;

    @Override
    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId) {
        return sseNotificationService.register(userId);
    }

    @Override
    @GetMapping(value = "/stream/user/{userId}/application/{application}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeByApplication(@PathVariable Long userId, @PathVariable String application) {
        return sseNotificationService.subscribe(userId, application);
    }

    @Override
    @PostMapping("/send")
    public ApiResponse<NotificationResponse> send(@RequestBody NotificationRequest request) {
        return new ApiResponse<>(notificationService.sendAsync(request));
    }

    @Override
    @PostMapping("/send/batch")
    public ApiResponse<List<NotificationResponse>> sendBatch(@RequestBody List<NotificationRequest> requests) {
        return new ApiResponse<>(notificationService.sendAsyncMultiple(requests));
    }

    @Override
    @PostMapping("/send/sync")
    public ApiResponse<NotificationResponse> sendSync(@RequestBody NotificationRequest request) {
        return new ApiResponse<>(notificationService.sendSync(request));
    }

    @Override
    @GetMapping
    public ApiResponse<Page<NotificationSummaryResponse>> getNotifications(@RequestParam(required = false) NotificationChannel channel,
                                                                           @ParameterObject Pageable pageable) {
        return new ApiResponse<>(notificationService.getNotifications(pageable, channel));
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<NotificationDetailResponse> getNotificationDetailById(@PathVariable Long id) {
        return new ApiResponse<>(notificationService.getNotificationDetailById(id));
    }

    @Override
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        return new ApiResponse<>(notificationService.getUnreadCount());
    }

    @Override
    @PostMapping("/device-tokens")
    public ApiResponse<Void> registerDeviceToken(@RequestBody DeviceTokenRequest request) {
        deviceTokenService.register(request);
        return new ApiResponse<>(null);
    }
}
