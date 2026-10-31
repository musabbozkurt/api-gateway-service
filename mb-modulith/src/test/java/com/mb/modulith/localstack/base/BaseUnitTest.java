package com.mb.modulith.localstack.base;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.data.model.TempTable3;
import io.reactivex.rxjava3.core.Flowable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class BaseUnitTest {

    public static StaticCredentialsProvider getAwsCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("awsAccessKey", "awsSecretKey"));
    }

    public static DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
        return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(getDynamoDbAsyncClient()).build();
    }

    public static DynamoDbAsyncClient getDynamoDbAsyncClient() {
        return DynamoDbAsyncClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    public static DynamoDbEnhancedClient getDynamoClient(URI endpoint, String region) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(getDynamoDbClient(endpoint, region)).build();
    }

    public static DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    public static AmazonDynamoDB amazonDynamoDB(String endpoint, String region) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .build();
    }

    public static DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(URI endpoint, String region) {
        return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(getDynamoDbAsyncClient(endpoint, region)).build();
    }

    public static DynamoDbAsyncClient getDynamoDbAsyncClient(URI endpoint, String region) {
        return DynamoDbAsyncClient.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    public static DynamoDbClient getDynamoDbClient(URI endpoint, String region) {
        return DynamoDbClient.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(getAwsCredentialsProvider())
                .build();
    }

    public static CreateTableRequest getTempTableRequest(DynamoDBMapper dynamoDBMapper) {
        CreateTableRequest tempTableRequest = dynamoDBMapper.generateCreateTableRequest(TempTable.class);
        tempTableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        return tempTableRequest;
    }

    public static CreateTableRequest getTempTableNameRequest() {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("N"));

        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH));

        return new CreateTableRequest()
                .withTableName("tempTableName")
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(6L));
    }

    public static software.amazon.awssdk.services.dynamodb.model.CreateTableRequest getTempTableNameRequest3() {
        List<software.amazon.awssdk.services.dynamodb.model.AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(software.amazon.awssdk.services.dynamodb.model.AttributeDefinition.builder().attributeName("Id").attributeType("N").build());

        List<software.amazon.awssdk.services.dynamodb.model.KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(software.amazon.awssdk.services.dynamodb.model.KeySchemaElement.builder().attributeName("Id").keyType(software.amazon.awssdk.services.dynamodb.model.KeyType.HASH).build());

        return software.amazon.awssdk.services.dynamodb.model.CreateTableRequest.builder()
                .tableName("tempTableName")
                .keySchema(keySchema)
                .attributeDefinitions(attributeDefinitions)
                .provisionedThroughput(software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(6L).build())
                .build();
    }

    public static List<Item> items() {
        return Arrays.asList(
                new Item().withPrimaryKey("Id", UUID.randomUUID().toString())
                        .withString("Color", "Blue")
                        .withString("ArticleId", "111-111111111")
                        .withBoolean("Status", Boolean.TRUE),
                new Item().withPrimaryKey("Id", UUID.randomUUID().toString())
                        .withString("Color", "Red")
                        .withString("ArticleId", "111-1111111112")
                        .withBoolean("Status", Boolean.TRUE),
                new Item().withPrimaryKey("Id", UUID.randomUUID().toString())
                        .withString("Color", "Yellow")
                        .withString("ArticleId", "111-1111111113")
                        .withBoolean("Status", Boolean.TRUE));
    }

    public static List<TempTable> getTempTableRecords() {
        return Arrays.asList(
                new TempTable(UUID.randomUUID().toString(), "Red", Boolean.TRUE),
                new TempTable(UUID.randomUUID().toString(), "Red", Boolean.TRUE),
                new TempTable(UUID.randomUUID().toString(), "Red", Boolean.FALSE));
    }

    public static List<TempTable3> getTempTable3Records() {
        return Arrays.asList(
                new TempTable3(UUID.randomUUID().toString(), "Red", Boolean.TRUE),
                new TempTable3(UUID.randomUUID().toString(), "Red", Boolean.TRUE),
                new TempTable3(UUID.randomUUID().toString(), "Red", Boolean.FALSE));
    }

    public static List<TempTable3> getTempTable3s(PagePublisher<TempTable3> publisher) {
        return Flowable.fromPublisher(publisher).flatMapIterable(Page::items).toList().blockingGet();
    }
}
