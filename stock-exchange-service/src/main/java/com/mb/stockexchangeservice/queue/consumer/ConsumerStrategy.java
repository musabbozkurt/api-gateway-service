package com.mb.stockexchangeservice.queue.consumer;

import com.mb.stockexchangeservice.queue.event.Event;

public interface ConsumerStrategy {

    void execute(Event event);

    boolean canExecute(Event event);
}
