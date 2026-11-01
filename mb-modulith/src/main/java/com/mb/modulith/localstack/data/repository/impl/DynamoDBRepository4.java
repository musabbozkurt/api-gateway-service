package com.mb.modulith.localstack.data.repository.impl;

import com.mb.modulith.localstack.data.repository.GenericRepository4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class DynamoDBRepository4<T> implements GenericRepository4<T> {

    protected final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<T> dynamoDbAsyncTable;

    private final Class<T> instanceClass;

    protected DynamoDBRepository4(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, Class<T> instanceClass) {
        this.instanceClass = instanceClass;
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.dynamoDbAsyncTable = dynamoDbEnhancedAsyncClient.table(instanceClass.getCanonicalName(), TableSchema.fromBean(instanceClass));
    }

    @Override
    public PagePublisher<T> findAll() {
        return dynamoDbAsyncTable.scan(ScanEnhancedRequest.builder().limit(100).build());
    }

    @Override
    public List<Key> deleteAll(List<T> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            WriteBatch.Builder<T> builder = WriteBatch.builder(instanceClass).mappedTableResource(dynamoDbAsyncTable);
            entities.forEach(builder::addDeleteItem);

            return dynamoDbEnhancedAsyncClient
                    .batchWriteItem(b -> b.writeBatches(builder.build()))
                    .join()
                    .unprocessedDeleteItemsForTable(dynamoDbAsyncTable);
        }
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<Key>> deleteAllAsync(List<T> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            WriteBatch.Builder<T> builder = WriteBatch.builder(instanceClass).mappedTableResource(dynamoDbAsyncTable);
            entities.forEach(builder::addDeleteItem);

            return dynamoDbEnhancedAsyncClient
                    .batchWriteItem(b -> b.writeBatches(builder.build()))
                    .handle((batchWriteResult, _) -> batchWriteResult.unprocessedDeleteItemsForTable(dynamoDbAsyncTable));
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public void createTable() {
        try {
            dynamoDbAsyncTable.createTable().join();
        } catch (Exception exception) {
            log.error("Exception occurred while creating table. createTable - Exception: {}", exception.getMessage());
        }
    }
}
