package com.mb.notificationservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.service.DeviceTokenService;
import com.mb.notificationservice.service.NotificationService;
import com.mb.notificationservice.service.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SseNotificationService sseNotificationService;

    @Mock
    private DeviceTokenService deviceTokenService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void send_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setLevel(NotificationLevel.INFO);
        request.setSubject("Test Subject");
        request.setBody("<p>Hello</p>");
        request.setRecipients(Set.of("user@example.com"));

        NotificationResponse response = NotificationResponse.builder()
                .channel(NotificationChannel.EMAIL)
                .success(true)
                .message("Notification queued successfully")
                .build();

        when(notificationService.sendAsync(any(NotificationRequest.class))).thenReturn(response);

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendAsync(any(NotificationRequest.class));
    }

    @Test
    void send_ShouldReturn400_WhenChannelIsNull() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendBatch_ShouldReturn200_WhenRequestsAreValid() throws Exception {
        // Arrange
        NotificationRequest request1 = new NotificationRequest();
        request1.setChannel(NotificationChannel.SMS);
        request1.setLevel(NotificationLevel.INFO);

        NotificationRequest request2 = new NotificationRequest();
        request2.setChannel(NotificationChannel.EMAIL);
        request2.setLevel(NotificationLevel.INFO);

        List<NotificationResponse> responses = List.of(
                NotificationResponse.builder().channel(NotificationChannel.SMS).success(true).build(),
                NotificationResponse.builder().channel(NotificationChannel.EMAIL).success(true).build()
        );

        when(notificationService.sendAsyncMultiple(anyList())).thenReturn(responses);

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/send/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request1, request2))))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendAsyncMultiple(anyList());
    }

    @Test
    void sendSync_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.PUSH);
        request.setLevel(NotificationLevel.INFO);
        request.setTitle("New Order");
        request.setBody("You have a new order");
        request.setUserId(12345L);
        request.setApplications(Set.of("app-one"));

        NotificationResponse response = NotificationResponse.builder()
                .channel(NotificationChannel.PUSH)
                .success(true)
                .message("Push notification sent successfully")
                .build();

        when(notificationService.sendSync(any(NotificationRequest.class))).thenReturn(response);

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/send/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendSync(any(NotificationRequest.class));
    }

    @Test
    void sendSync_ShouldReturn400_WhenChannelIsNull() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/send/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribe_ShouldReturn200_WhenUserIdIsProvided() throws Exception {
        // Arrange
        when(sseNotificationService.register(anyLong())).thenReturn(new SseEmitter());

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications/stream/{userId}", 123456L)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).register(anyLong());
    }

    @Test
    void subscribeByApplication_ShouldReturn200_WhenUserIdAndApplicationAreProvided() throws Exception {
        // Arrange
        when(sseNotificationService.subscribe(anyLong(), anyString())).thenReturn(new SseEmitter());

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications/stream/user/{userId}/application/{application}", 123456L, "my-application")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());

        verify(sseNotificationService, times(1)).subscribe(anyLong(), anyString());
    }

    @Test
    void getNotifications_ShouldReturn200_WhenChannelIsNotProvided() throws Exception {
        // Arrange
        NotificationSummaryResponse summary = NotificationSummaryResponse.builder()
                .id(1L)
                .channel(NotificationChannel.PUSH)
                .subject("Order Notification")
                .title("New Order")
                .level(NotificationLevel.INFO)
                .status(NotificationStatus.SENT)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Page<NotificationSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        when(notificationService.getNotifications(any(), any())).thenReturn(page);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotifications(any(), any());
    }

    @Test
    void getNotifications_ShouldReturn200_WhenChannelFilterIsProvided() throws Exception {
        // Arrange
        NotificationSummaryResponse summary = NotificationSummaryResponse.builder()
                .id(1L)
                .channel(NotificationChannel.EMAIL)
                .subject("Order Confirmation")
                .title("Order Confirmation")
                .level(NotificationLevel.INFO)
                .status(NotificationStatus.SENT)
                .read(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<NotificationSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        when(notificationService.getNotifications(any(), any())).thenReturn(page);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications").param("channel", "EMAIL"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotifications(any(), any());
    }

    @Test
    void getNotificationDetailById_ShouldReturn200_WhenNotificationExists() throws Exception {
        // Arrange
        NotificationDetailResponse detail = NotificationDetailResponse.builder()
                .id(1L)
                .channel(NotificationChannel.PUSH)
                .level(NotificationLevel.INFO)
                .title("New Order")
                .body("You have a new order to review")
                .status(NotificationStatus.SENT)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.getNotificationDetailById(1L)).thenReturn(detail);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications/{id}", 1L))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotificationDetailById(1L);
    }

    @Test
    void getUnreadCount_ShouldReturn200_WhenUserIsAuthenticated() throws Exception {
        // Arrange
        when(notificationService.getUnreadCount()).thenReturn(3L);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getUnreadCount();
    }

    @Test
    void registerDeviceToken_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        String requestBody = """
                {
                  "token": "fcm-device-token-abc123",
                  "platform": "ANDROID",
                  "application": "my-application"
                }""";

        doNothing().when(deviceTokenService).register(any());

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(deviceTokenService, times(1)).register(any());
    }

    @Test
    void registerDeviceToken_ShouldReturn400_WhenTokenIsBlank() throws Exception {
        // Arrange
        String requestBody = """
                {
                  "token": "",
                  "platform": "ANDROID",
                  "application": "my-application"
                }""";

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerDeviceToken_ShouldReturn400_WhenPlatformIsNull() throws Exception {
        // Arrange
        String requestBody = """
                {
                  "token": "fcm-device-token-abc123",
                  "application": "my-application"
                }""";

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/notifications/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
