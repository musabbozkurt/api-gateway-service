package com.mb.studentservice.queue.strategy;

import org.springframework.messaging.Message;

public interface EventConsumerStrategy {

    void execute(Message<?> event);

    boolean canExecute(String userEventType);
}
