package com.mb.swagger2.queue.producer;

import com.mb.swagger2.enums.EventType;

public interface Swagger2EventProducer {

    void publishEvent(String message, EventType eventType);

}
