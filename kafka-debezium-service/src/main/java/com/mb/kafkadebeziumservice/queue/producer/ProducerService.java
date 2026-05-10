package com.mb.kafkadebeziumservice.queue.producer;

public interface ProducerService {

    void publishMessage(String topicName, String message);
}
