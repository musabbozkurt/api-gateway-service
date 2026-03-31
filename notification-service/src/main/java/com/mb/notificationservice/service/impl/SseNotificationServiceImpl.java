package com.mb.notificationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.SseNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseNotificationServiceImpl implements SseNotificationService {

    private final ObjectWriter compactWriter;
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseNotificationServiceImpl(ObjectMapper objectMapper) {
        this.compactWriter = objectMapper.writer().without(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        List<SseEmitter> emitters = userEmitters.computeIfAbsent(userId, _ -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(_ -> removeEmitter(userId, emitter));

        sendConnectedEvent(userId, emitter);

        return emitter;
    }

    @Override
    public void send(NotificationEventDto notification) {
        if (Objects.nonNull(notification.getUserId())) {
            sendToUser(notification.getUserId(), notification);
        } else {
            broadcast(notification);
        }
    }

    private void sendToUser(Long userId, Object data) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.forEach(emitter -> doSend(userId, emitter, data));
        }
    }

    private void broadcast(Object data) {
        userEmitters.forEach((userId, emitters) -> emitters.forEach(emitter -> doSend(userId, emitter, data)));
    }

    // Disable spring.jackson.serialization.indent-output: true or add compactWriter to application context to avoid sending pretty-printed JSON which can cause issues with SSE clients
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        userEmitters.forEach((userId, emitters) ->
                emitters.forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } catch (IOException _) {
                        removeEmitter(userId, emitter);
                    }
                })
        );
    }

    private void doSend(Long userId, SseEmitter emitter, Object data) {
        try {
            emitter.send(SseEmitter.event().data(compactWriter.writeValueAsString(data)));
        } catch (IOException _) {
            log.warn("SSE send failed for userId: {}. Removing emitter.", userId);
            removeEmitter(userId, emitter);
        }
    }

    private void sendConnectedEvent(Long userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("connected").data("userId=" + userId));
        } catch (IOException _) {
            log.warn("Failed to send connected event for userId: {}", userId);
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (!CollectionUtils.isEmpty(emitters)) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
        log.debug("SSE emitter cleaned up for userId: {}", userId);
    }
}
