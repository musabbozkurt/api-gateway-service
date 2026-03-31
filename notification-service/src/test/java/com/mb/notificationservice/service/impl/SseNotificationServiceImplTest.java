package com.mb.notificationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SseNotificationServiceImplTest {

    private SseNotificationServiceImpl sseService;

    @BeforeEach
    void setUp() {
        sseService = new SseNotificationServiceImpl(new ObjectMapper());
    }

    @SuppressWarnings("unchecked")
    private Map<Long, List<SseEmitter>> getUserEmitters() {
        return (Map<Long, List<SseEmitter>>) ReflectionTestUtils.getField(sseService, "userEmitters");
    }

    @Test
    void register_ShouldReturnEmitterAndStoreIt_WhenUserIdIsProvided() {
        SseEmitter emitter = sseService.register(1L);

        assertNotNull(emitter);
        assertTrue(getUserEmitters().containsKey(1L));
        assertEquals(1, getUserEmitters().get(1L).size());
    }

    @Test
    void send_ShouldSendToAllEmittersOfUser_WhenUserIdIsSet() throws IOException {
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        getUserEmitters().put(1L, new CopyOnWriteArrayList<>(List.of(emitter1, emitter2)));

        NotificationEventDto dto = new NotificationEventDto();
        dto.setUserId(1L);
        dto.setChannel(NotificationChannel.PUSH);

        sseService.send(dto);

        verify(emitter1).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void send_ShouldBroadcastToAll_WhenUserIdIsNull() throws IOException {
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);
        getUserEmitters().put(1L, new CopyOnWriteArrayList<>(List.of(emitter1)));
        getUserEmitters().put(2L, new CopyOnWriteArrayList<>(List.of(emitter2)));

        NotificationEventDto dto = new NotificationEventDto();
        dto.setUserId(null);
        dto.setChannel(NotificationChannel.PUSH);

        sseService.send(dto);

        verify(emitter1).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void send_ShouldRemoveOnlyFailedEmitter_WhenSendThrowsIOException() throws IOException {
        SseEmitter brokenEmitter = mock(SseEmitter.class);
        SseEmitter healthyEmitter = mock(SseEmitter.class);
        doThrow(new IOException("broken pipe")).when(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));
        getUserEmitters().put(1L, new CopyOnWriteArrayList<>(List.of(brokenEmitter, healthyEmitter)));

        NotificationEventDto dto = new NotificationEventDto();
        dto.setUserId(1L);
        dto.setChannel(NotificationChannel.PUSH);

        sseService.send(dto);

        assertTrue(getUserEmitters().containsKey(1L));
        assertEquals(1, getUserEmitters().get(1L).size());
    }

    @Test
    void send_ShouldRemoveUserEntry_WhenAllEmittersFail() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("broken pipe")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        getUserEmitters().put(1L, new CopyOnWriteArrayList<>(List.of(emitter)));

        NotificationEventDto dto = new NotificationEventDto();
        dto.setUserId(1L);
        dto.setChannel(NotificationChannel.PUSH);

        sseService.send(dto);

        assertFalse(getUserEmitters().containsKey(1L));
    }
}
