package com.mb.modulith.localstack.data.repository.impl;

import com.mb.modulith.localstack.data.repository.GenericRepository3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch.Builder;

import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class DynamoDBRepository3<T> implements GenericRepository3<T> {

    protected final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    protected final DynamoDbTable<T> dynamoDbTable;

    private final Class<T> instanceClass;

    protected DynamoDBRepository3(DynamoDbEnhancedClient dynamoDbEnhancedClient, Class<T> instanceClass) {
        this.instanceClass = instanceClass;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.dynamoDbTable = this.dynamoDbEnhancedClient.table(instanceClass.getCanonicalName(), TableSchema.fromBean(instanceClass));
    }

    @Override
    public PageIterable<T> findAll() {
        return dynamoDbTable.scan(ScanEnhancedRequest.builder().limit(100).build());
    }

    @Override
    public List<Key> deleteAll(List<T> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            Builder<T> builder = WriteBatch.builder(instanceClass).mappedTableResource(dynamoDbTable);
            entities.forEach(builder::addDeleteItem);

            return dynamoDbEnhancedClient
                    .batchWriteItem(b -> b.writeBatches(builder.build()))
                    .unprocessedDeleteItemsForTable(dynamoDbTable);
        }
        return Collections.emptyList();
    }
}
