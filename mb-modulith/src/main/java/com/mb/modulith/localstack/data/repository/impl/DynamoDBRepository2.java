package com.mb.modulith.localstack.data.repository.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.mb.modulith.localstack.data.repository.GenericRepository2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.List;

@Slf4j
public abstract class DynamoDBRepository2<T> implements GenericRepository2<T> {

    protected final AmazonDynamoDB amazonDynamoDB;
    protected final DynamoDB dynamoDB;
    protected final DynamoDbClient dynamoDbClient;
    protected final DynamoDBMapper mapper;

    private final Class<T> instanceClass;

    protected DynamoDBRepository2(AmazonDynamoDB amazonDynamoDB, DynamoDbClient dynamoDbClient, Class<T> instanceClass) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDB = new DynamoDB(amazonDynamoDB);
        this.dynamoDbClient = dynamoDbClient;
        this.mapper = new DynamoDBMapper(amazonDynamoDB);
        this.instanceClass = instanceClass;
    }

    @Override
    public PaginatedScanList<T> findAll() {
        return mapper.scan(instanceClass, new DynamoDBScanExpression());
    }

    @Override
    public void deleteAll(List<T> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            mapper.batchDelete(entities);
        }
    }

    @Override
    public GetItemResponse getItem(GetItemRequest itemRequest) {
        log.info("GetItemRequest: {}", itemRequest);
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        log.info("GetItemResponse: {}", response);
        return response;
    }

    @Override
    public void createTableIfNotExists(Class<?> clazz) {
        CreateTableRequest tableCarRequest = mapper.generateCreateTableRequest(clazz);
        tableCarRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        TableUtils.createTableIfNotExists(amazonDynamoDB, tableCarRequest);
        try {
            TableUtils.waitUntilActive(amazonDynamoDB, clazz.getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for table to become active: " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public void loadSampleItems(String tableName, List<T> items) {
        log.info("Adding data to {}", tableName);
        mapper.batchSave(items);
    }
}
