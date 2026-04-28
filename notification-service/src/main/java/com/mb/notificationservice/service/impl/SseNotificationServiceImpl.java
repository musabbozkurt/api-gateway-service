package com.mb.notificationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.SseNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseNotificationServiceImpl implements SseNotificationService {

    private final ObjectWriter compactWriter;
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseNotificationServiceImpl(ObjectMapper objectMapper) {
        this.compactWriter = objectMapper.writer().without(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public SseEmitter register(Long userId) {
        return subscribe(userId, "default");
    }

    @Override
    public SseEmitter subscribe(Long userId, String application) {
        String key = buildKey(userId, application);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        List<SseEmitter> emitterList = emitters.computeIfAbsent(key, _ -> new CopyOnWriteArrayList<>());
        emitterList.add(emitter);

        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key, emitter));
        emitter.onError(_ -> removeEmitter(key, emitter));

        sendConnectedEvent(key, emitter);

        return emitter;
    }

    @Override
    public void send(NotificationEventDto notification) {
        Set<String> applications = notification.getApplications();
        if (Objects.nonNull(notification.getUserId()) && CollectionUtils.isNotEmpty(applications)) {
            for (String application : applications) {
                String key = buildKey(notification.getUserId(), application);
                sendToKey(key, notification);
            }
        } else if (CollectionUtils.isNotEmpty(notification.getApplications())) {
            sendToApplications(notification.getApplications(), notification);
        } else if (Objects.nonNull(notification.getUserId())) {
            sendToUser(notification.getUserId(), notification);
        } else {
            broadcast(notification);
        }
    }

    private void sendToKey(String key, Object data) {
        List<SseEmitter> emitterList = emitters.get(key);
        if (!CollectionUtils.isEmpty(emitterList)) {
            emitterList.forEach(emitter -> doSend(key, emitter, data));
        }
    }

    private void sendToUser(Long userId, Object data) {
        String prefix = userId + ":";
        emitters.forEach((key, emitterList) -> {
            if (key.startsWith(prefix)) {
                emitterList.forEach(emitter -> doSend(key, emitter, data));
            }
        });
    }

    private void sendToApplications(Set<String> applications, Object data) {
        emitters.forEach((key, emitterList) -> {
            for (String application : applications) {
                String suffix = ":" + application;
                if (key.endsWith(suffix)) {
                    emitterList.forEach(emitter -> doSend(key, emitter, data));
                    break;
                }
            }
        });
    }

    private void broadcast(Object data) {
        emitters.forEach((key, emitterList) -> emitterList.forEach(emitter -> doSend(key, emitter, data)));
    }

    // Disable spring.jackson.serialization.indent-output: true or add compactWriter to application context to avoid sending pretty-printed JSON which can cause issues with SSE clients
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        emitters.forEach((key, emitterList) ->
                emitterList.forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } catch (IOException _) {
                        removeEmitter(key, emitter);
                    }
                })
        );
    }

    private void doSend(String key, SseEmitter emitter, Object data) {
        try {
            emitter.send(SseEmitter.event().data(compactWriter.writeValueAsString(data)));
        } catch (IOException _) {
            log.warn("SSE send failed for key: {}. Removing emitter.", key);
            removeEmitter(key, emitter);
        }
    }

    private void sendConnectedEvent(String key, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("connected").data("key=" + key));
        } catch (IOException _) {
            log.warn("Failed to send connected event for key: {}", key);
        }
    }

    private void removeEmitter(String key, SseEmitter emitter) {
        List<SseEmitter> emitterList = emitters.get(key);
        if (!CollectionUtils.isEmpty(emitterList)) {
            emitterList.remove(emitter);
            if (emitterList.isEmpty()) {
                emitters.remove(key);
            }
        }
        log.debug("SSE emitter cleaned up for key: {}", key);
    }

    private String buildKey(Long userId, String application) {
        return userId + ":" + application;
    }
}
