package com.mb.notificationservice.config;

import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.util.ServiceConstants;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.flyway.enabled=false",
                "spring.sql.init.mode=never"
        }
)
@EmbeddedKafka(partitions = 1, topics = {ServiceConstants.NOTIFICATION_TOPIC})
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, NotificationEventDto> kafkaTemplate;

    @Autowired
    // This is just an IntelliJ inspection false positive.
    // The EmbeddedKafkaBroker bean is registered at runtime by the @EmbeddedKafka annotation, so IntelliJ can't see it at compile time.
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Consumer<String, byte[]> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        DefaultKafkaConsumerFactory<String, byte[]> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(ServiceConstants.NOTIFICATION_TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void testSendMessage() {
        NotificationEventDto eventDto = new NotificationEventDto();
        eventDto.setBody("Test message");

        kafkaTemplate.send(ServiceConstants.NOTIFICATION_TOPIC, eventDto);

        ConsumerRecord<String, byte[]> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, ServiceConstants.NOTIFICATION_TOPIC, Duration.ofSeconds(5));

        assertNotNull(consumerRecord);
        assertEquals(ServiceConstants.NOTIFICATION_TOPIC, consumerRecord.topic());
        assertNotNull(consumerRecord.value());
    }

    @Test
    void testSendMessageWithKey() {
        String testKey = "test-key";
        NotificationEventDto eventDto = new NotificationEventDto();
        eventDto.setBody("Test message with key");

        kafkaTemplate.send(ServiceConstants.NOTIFICATION_TOPIC, testKey, eventDto);

        ConsumerRecord<String, byte[]> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, ServiceConstants.NOTIFICATION_TOPIC, Duration.ofSeconds(5));

        assertNotNull(consumerRecord);
        assertEquals(ServiceConstants.NOTIFICATION_TOPIC, consumerRecord.topic());
        assertEquals(testKey, consumerRecord.key());
        assertNotNull(consumerRecord.value());
    }
}
