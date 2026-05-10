package com.mb.kafkadebeziumservice.queue.consumer.impl;

import com.mb.kafkadebeziumservice.config.KafkaConsumerConfig;
import com.mb.kafkadebeziumservice.queue.consumer.ConsumerService;
import com.mb.kafkadebeziumservice.queue.event.consumer.Customer;
import com.mb.kafkadebeziumservice.queue.event.consumer.DebeziumCustomerMessage;
import com.mb.kafkadebeziumservice.queue.event.consumer.DebeziumProductMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final KafkaConsumerConfig consumerConfig;
    private final KafkaConsumer<Integer, String> ordersConsumer;

    public ConsumerServiceImpl(KafkaConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
        this.ordersConsumer = consumerConfig.getOrdersConsumer();
    }

    @KafkaListener(topics = "${spring.kafka.topics.topic1}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) String receivedPartitionId,
                        @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("Received a request to consume topic1 message. receivedPartitionId: {} offset: {} message: {}", receivedPartitionId, offset, message);
    }

    @KafkaListener(topics = {"${spring.kafka.topics.topic2}", "${spring.kafka.topics.topic5}"}, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryCustomers(@Payload(required = false) DebeziumCustomerMessage message,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION) String receivedPartitionId,
                                          @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("Received a request to consume topic2 message. receivedPartitionId: {} offset: {} message: {}", receivedPartitionId, offset, message);

        if (message == null) {
            log.info("Received tombstone customer record (null value) - this is normal for DELETE operations");
            return;
        }

        DebeziumCustomerMessage.Payload payload = message.getPayload();
        if (payload == null) {
            log.info("Received customer message with null payload");
            return;
        }

        processPayload(payload);
    }

    @KafkaListener(topics = "${spring.kafka.topics.topic4}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryProducts(@Payload(required = false) DebeziumProductMessage message,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) String receivedPartitionId,
                                         @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("Received a request to consume topic4 message. receivedPartitionId: {} offset: {} message: {}", receivedPartitionId, offset, message);

        if (message == null) {
            log.info("Received tombstone product record (null value) - this is normal for DELETE operations");
            return;
        }

        DebeziumProductMessage.Payload payload = message.getPayload();
        if (payload == null) {
            log.info("Received product message with null payload");
            return;
        }

        processPayload(payload);
    }

    @KafkaListener(topics = "${spring.kafka.topics.topic3}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCustomers(Customer customer,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) String receivedPartitionId,
                                 @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("Received a request to consume customer. receivedPartitionId: {} offset: {} customer: {}", receivedPartitionId, offset, customer);
    }

    @Override
    public List<String> consumeOrders() {
        log.info("starting message consumption for topic: {}", ordersConsumer.subscription());
        ConsumerRecords<Integer, String> records = consumerConfig.getOrdersConsumer().poll(Duration.ofMillis(1000));
        List<String> response = new ArrayList<>();
        for (ConsumerRecord<Integer, String> consumerRecord : records) {
            log.info("Received message: ({}) at offset {}", consumerRecord.value(), consumerRecord.offset());
            response.add(consumerRecord.value());
        }
        return response;
    }

    private void processPayload(DebeziumCustomerMessage.Payload payload) {
        String operation = payload.getOp();

        switch (operation) {
            case "c", "u", "r" -> {
                DebeziumCustomerMessage.Payload.CustomerData customer = payload.getAfter();
                if (customer != null) {
                    log.info("Customer data - ID: {}, Name: {} {}, Email: {}", customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getEmail());
                }
            }
            case "d" -> {
                DebeziumCustomerMessage.Payload.CustomerData customer = payload.getBefore();
                if (customer != null) {
                    log.info("Deleted customer - ID: {}", customer.getId());
                }
            }
            default -> log.warn("Unknown customer operation type: {}", operation);
        }
    }

    private void processPayload(DebeziumProductMessage.Payload payload) {
        String operation = payload.getOp();

        switch (operation) {
            case "c", "u", "r" -> {
                DebeziumProductMessage.Payload.ProductData product = payload.getAfter();
                if (product != null) {
                    log.info("Product data - ID: {}, Name: {} {}", product.getId(), product.getDescription(), product.getWeight());
                }
            }
            case "d" -> {
                DebeziumProductMessage.Payload.ProductData product = payload.getBefore();
                if (product != null) {
                    log.info("Deleted product - ID: {}", product.getId());
                }
            }
            default -> log.warn("Unknown product operation type: {}", operation);
        }
    }
}
