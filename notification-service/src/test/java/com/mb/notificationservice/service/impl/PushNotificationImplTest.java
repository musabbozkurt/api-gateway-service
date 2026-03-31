package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.SseNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PushNotificationImplTest {

    @InjectMocks
    private PushNotificationImpl pushNotification;

    @Mock
    private SseNotificationService sseSender;

    @Test
    void send_ShouldReturnSuccessResponse_WhenSseSendSucceeds() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("New Order");
        request.setBody("You have a new order");
        request.setData(Map.of("orderId", "123"));

        NotificationResponse response = pushNotification.send(request);

        assertNotNull(response.getId());
        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertTrue(response.isSuccess());
        verify(sseSender).send(any());
    }

    @Test
    void send_ShouldReturnFailureResponse_WhenSseSendThrowsException() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setTitle("Title");
        request.setBody("Body");

        doThrow(new RuntimeException("connection lost")).when(sseSender).send(any());

        NotificationResponse response = pushNotification.send(request);

        assertNotNull(response.getId());
        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("connection lost"));
    }

    @Test
    void getChannel_ShouldReturnPush_WhenCalled() {
        assertEquals(NotificationChannel.PUSH, pushNotification.getChannel());
    }
}
