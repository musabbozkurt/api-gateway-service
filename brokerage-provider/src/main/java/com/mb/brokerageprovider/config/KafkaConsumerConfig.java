package com.mb.brokerageprovider.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    private KafkaConsumer<Integer, String> ordersConsumer;

    @Bean
    public KafkaConsumer<Integer, String> getOrdersConsumer() {
        if (ordersConsumer == null) {
            Properties props = new Properties();
            props.setProperty("key.deserializer", keyDeserializer);
            props.setProperty("value.deserializer", valueDeserializer);
            props.setProperty("bootstrap.servers", bootstrapServers);
            props.setProperty("group.id", groupId);
            ordersConsumer = new KafkaConsumer<>(props);
            ordersConsumer.subscribe(Collections.singletonList("orders"));
        }
        return ordersConsumer;
    }
}
