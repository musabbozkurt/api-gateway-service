package com.mb.modulith.localstack.data.repository.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.mb.modulith.localstack.data.repository.GenericRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class DynamoDBRepository<T, I> implements GenericRepository<T, I> {

    protected final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapper mapper;

    private final Class<T> instanceClass;

    protected DynamoDBRepository(AmazonDynamoDB amazonDynamoDB, Class<T> instanceClass) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.instanceClass = instanceClass;
        this.mapper = new DynamoDBMapper(amazonDynamoDB);
    }

    @Override
    public List<T> findAll() {
        return mapper.scan(instanceClass, new DynamoDBScanExpression());
    }

    @Override
    public Optional<T> findById(I id) {
        T entity = mapper.load(instanceClass, id);

        if (entity != null) {
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    @Override
    public T save(T entity) {
        mapper.save(entity);
        return entity;
    }

    @Override
    public void delete(T entity) {
        // Delete by key: mapper.delete(entity) sends an empty ConditionExpression, which current LocalStack rejects.
        DynamoDBMapperTableModel<T> tableModel = mapper.getTableModel(instanceClass);
        amazonDynamoDB.deleteItem(mapper.generateCreateTableRequest(instanceClass).getTableName(), tableModel.convertKey(entity));
    }

    public void deleteAll(List<T> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            mapper.batchDelete(entities);
        }
    }

    @Override
    public List<T> findAllWithLimit(int limit) {
        return mapper
                .scanPage(instanceClass, new DynamoDBScanExpression().withLimit(limit))
                .getResults();
    }
}
