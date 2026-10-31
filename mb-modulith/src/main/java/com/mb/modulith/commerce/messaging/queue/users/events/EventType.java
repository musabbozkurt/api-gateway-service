package com.mb.modulith.commerce.messaging.queue.users.events;

import lombok.Getter;

import java.util.Arrays;

public enum EventType {

    USER_CREATED(UserCreatedEvent.class);

    @Getter
    private final Class<? extends Event> eventClass;

    EventType(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    public static EventType getType(String eventType) {
        return Arrays.stream(values()).filter(enumEventType -> enumEventType.name().equals(eventType))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unused")
    public static boolean exists(String eventType) {
        return Arrays.stream(values()).anyMatch(enumEventType -> enumEventType.name().equals(eventType));
    }
}
