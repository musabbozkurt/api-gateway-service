package com.mb.kafkadebeziumservice.queue.producer.impl;

import com.mb.kafkadebeziumservice.queue.event.producer.Customer;
import com.mb.kafkadebeziumservice.queue.producer.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.topic3}")
    private String topic3;

    @Override
    public void publishMessage(String topicName, String message) {
        log.info("Publishing message to Kafka topic : {}, Object: {}", topicName, message);
        kafkaTemplate.send(topic3, new Customer(1L, "firstName", "lastName", "email@email.com"));
        CompletableFuture<SendResult<String, Object>> send = kafkaTemplate.send(topicName, message);
        send.whenComplete((sendResult, exception) -> {
            if (exception != null) {
                send.completeExceptionally(exception);
            } else {
                send.complete(sendResult);
            }
            log.info("Message published to topic: {}, partition: {}, offset: {}", topicName, sendResult.getRecordMetadata().partition(), sendResult.getRecordMetadata().offset());
        });
    }
}
