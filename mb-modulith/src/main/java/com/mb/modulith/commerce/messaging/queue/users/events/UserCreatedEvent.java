package com.mb.modulith.commerce.messaging.queue.users.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserCreatedEvent implements Event {

    @Builder.Default
    private EventType eventType = EventType.USER_CREATED;
}
