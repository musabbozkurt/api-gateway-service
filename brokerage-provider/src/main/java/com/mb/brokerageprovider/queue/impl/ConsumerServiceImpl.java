package com.mb.brokerageprovider.queue.impl;

import com.mb.brokerageprovider.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl {

    private final OrderService orderService;

    @KafkaListener(topics = "${spring.kafka.topics.topic1}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String orderId,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) String receivedPartitionId,
                        @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("Received a request to consume orderId. receivedPartitionId: {} offset: {} orderId: {}", receivedPartitionId, offset, orderId);
        orderService.updateOrder(Long.valueOf(orderId));
    }
}
