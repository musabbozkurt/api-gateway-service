package com.mb.swaggerapplication.queue.producer;

import com.mb.swaggerapplication.enums.EventType;

public interface SwaggerEventProducer {

    void publishEvent(String message, EventType eventType);

}
