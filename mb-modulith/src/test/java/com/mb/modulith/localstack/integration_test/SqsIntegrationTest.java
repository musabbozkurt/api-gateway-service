package com.mb.modulith.localstack.integration_test;

import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SqsIntegrationTest {

    @Container
    private static final FlociContainer floci = new FlociContainer();

    private SqsClient sqs;

    @BeforeEach
    void setUp() {
        sqs = SqsClient.builder()
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .build();
    }

    @Test
    void createQueue_ShouldSuccessfullyCreateQueue_WhenQueueNameIsValid() {
        // Arrange
        String queueName = "orders";

        // Act
        String queueUrl = sqs.createQueue(b -> b.queueName(queueName)).queueUrl();

        // Assertions
        assertThat(queueUrl).contains(queueName);
    }

    @Test
    void sendMessage_ShouldDeliverMessageToQueue_WhenQueueExists() {
        // Arrange
        String queueUrl = sqs.createQueue(b -> b.queueName("orders")).queueUrl();
        String messageBody = "{\"event\":\"order.placed\"}";

        // Act
        sqs.sendMessage(b -> b.queueUrl(queueUrl).messageBody(messageBody));

        // Assertions
        List<Message> messages = sqs.receiveMessage(b -> b.queueUrl(queueUrl).maxNumberOfMessages(1)).messages();
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().body()).isEqualTo(messageBody);
    }

    @Test
    void deleteMessage_ShouldRemoveMessageFromQueue_WhenMessageIsAcknowledged() {
        // Arrange
        String queueUrl = sqs.createQueue(b -> b.queueName("notifications")).queueUrl();
        sqs.sendMessage(b -> b.queueUrl(queueUrl).messageBody("temp-message"));
        List<Message> receivedMessages = sqs.receiveMessage(b -> b.queueUrl(queueUrl).maxNumberOfMessages(1)).messages();
        assertThat(receivedMessages).hasSize(1);
        String receiptHandle = receivedMessages.getFirst().receiptHandle();

        // Act
        sqs.deleteMessage(b -> b.queueUrl(queueUrl).receiptHandle(receiptHandle));

        // Assertions
        List<Message> messagesAfterDelete = sqs.receiveMessage(b -> b.queueUrl(queueUrl).maxNumberOfMessages(1).visibilityTimeout(0)).messages();
        assertThat(messagesAfterDelete).isEmpty();
    }
}
