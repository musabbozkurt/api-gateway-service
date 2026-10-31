package com.mb.modulith.commerce.messaging.queue;

import org.springframework.messaging.Message;

public interface ConsumerEventStrategy {

    void execute(Message<?> event);

    boolean canExecute(String eventType);
}
