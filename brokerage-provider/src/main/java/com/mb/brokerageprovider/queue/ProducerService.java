package com.mb.brokerageprovider.queue;

public interface ProducerService {

    void publishMessage(String topicName, String orderId);
}
