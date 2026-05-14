package com.mb.brokerageprovider.queue.impl;

import com.mb.brokerageprovider.queue.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishMessage(String topicName, String orderId) {
        this.kafkaTemplate.send(topicName, orderId)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("orderId: {} is sent with offset: {}", orderId, result.getRecordMetadata().offset());
                    } else {
                        log.error("Unable to send orderId: {} due to: {}", orderId, throwable.getMessage());
                    }
                });
    }
}
