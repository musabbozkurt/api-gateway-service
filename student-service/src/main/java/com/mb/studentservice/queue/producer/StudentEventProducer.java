package com.mb.studentservice.queue.producer;

import com.mb.studentservice.enums.EventType;

public interface StudentEventProducer {

    void publishEvent(String message, EventType eventType);

}
