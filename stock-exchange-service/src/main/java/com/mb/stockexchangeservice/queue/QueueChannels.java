package com.mb.stockexchangeservice.queue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueChannels {
    public static final String INTERNAL_EVENT_PRODUCER = "internalEventProducer-in-0";
    public static final String USER_CREATED_EVENT_PRODUCER = "userCreatedEvent-in-0";
}
