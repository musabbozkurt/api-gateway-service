package com.mb.swagger2.queue.strategy;

import org.springframework.messaging.Message;

public interface EventConsumerStrategy {

    void execute(Message<?> event);

    boolean canExecute(String userEventType);
}
