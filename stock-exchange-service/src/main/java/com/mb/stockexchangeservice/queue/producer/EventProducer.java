package com.mb.stockexchangeservice.queue.producer;

import com.mb.stockexchangeservice.queue.event.Event;

public interface EventProducer {

    void publishEvent(String bindingName, Event event);
}
