package com.mb.kafkadebeziumservice.queue.consumer;

import java.util.List;

public interface ConsumerService {

    List<String> consumeOrders();
}
