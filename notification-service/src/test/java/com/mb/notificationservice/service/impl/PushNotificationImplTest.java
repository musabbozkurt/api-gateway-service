package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.client.firebase.service.FcmService;
import com.mb.notificationservice.data.entity.DeviceToken;
import com.mb.notificationservice.enums.DevicePlatform;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.DeviceTokenService;
import com.mb.notificationservice.service.SseNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationImplTest {

    @InjectMocks
    private PushNotificationImpl pushNotification;

    @Mock
    private SseNotificationService sseNotificationService;

    @Mock
    private FcmService fcmService;

    @Mock
    private DeviceTokenService deviceTokenService;

    @Test
    void send_ShouldReturnFailure_WhenNoApplicationsSpecified() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("New Order");
        request.setBody("You have a new order");

        NotificationResponse response = pushNotification.send(request);

        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No applications specified"));
    }

    @Test
    void send_ShouldReturnSuccess_WhenActiveDeviceTokensExist() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("New Order");
        request.setBody("You have a new order");
        request.setApplications(Set.of("my-app"));
        request.setData(Map.of("orderId", "123"));

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setToken("fcm-token");
        deviceToken.setApplication("my-app");
        deviceToken.setPlatform(DevicePlatform.ANDROID);

        when(deviceTokenService.getActiveTokensByUserIdAndApplications(anyLong(), anySet())).thenReturn(List.of(deviceToken));

        NotificationResponse response = pushNotification.send(request);

        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertTrue(response.isSuccess());
        verify(sseNotificationService).send(any());
        verify(fcmService).send(any(), eq("my-app"));
    }

    @Test
    void send_ShouldReturnFailure_WhenNoActiveDeviceTokens() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("Title");
        request.setBody("Body");
        request.setApplications(Set.of("my-app"));

        when(deviceTokenService.getActiveTokensByUserIdAndApplications(anyLong(), anySet())).thenReturn(List.of());

        NotificationResponse response = pushNotification.send(request);

        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("no active Firebase device token"));
    }

    @Test
    void send_ShouldReturnFailureResponse_WhenExceptionOccurs() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("Title");
        request.setBody("Body");
        request.setApplications(Set.of("my-app"));

        doThrow(new RuntimeException("connection lost")).when(sseNotificationService).send(any());

        NotificationResponse response = pushNotification.send(request);

        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("connection lost"));
    }

    @Test
    void getChannel_ShouldReturnPush_WhenCalled() {
        assertEquals(NotificationChannel.PUSH, pushNotification.getChannel());
    }
}
