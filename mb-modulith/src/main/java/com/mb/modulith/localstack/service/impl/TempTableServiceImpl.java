package com.mb.modulith.localstack.service.impl;

import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.data.model.TempTable3;
import com.mb.modulith.localstack.data.repository.TempTableRepository;
import com.mb.modulith.localstack.data.repository.TempTableRepository4;
import com.mb.modulith.localstack.service.TempTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempTableServiceImpl implements TempTableService {

    private final TempTableRepository tempTableRepository;
    private final TempTableRepository4 tempTableRepository4;

    @Override
    public Iterable<TempTable> getTempTableRecords() {
        return tempTableRepository.findAll();
    }

    @Override
    public void deleteTempTableRecords(List<TempTable> entities) {
        tempTableRepository.deleteAll(entities);
    }

    @Override
    public GetItemResponse getItem(String id) {
        try {
            HashMap<String, AttributeValue> keyToGet = new HashMap<>();
            keyToGet.put("Id", AttributeValue.builder().s(id).build());

            return tempTableRepository.getItem(GetItemRequest.builder().key(keyToGet).tableName(TempTable.class.getSimpleName()).build());
        } catch (DynamoDbException e) {
            log.error("Exception occurred while getting item. getItem - Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return GetItemResponse.builder().build();
    }

    @Override
    public void createTableIfNotExists() {
        tempTableRepository.createTableIfNotExists(TempTable.class);
    }

    @Override
    public void loadSampleItems() {
        try {
            tempTableRepository.loadSampleItems(TempTable.class.getSimpleName(),
                    Arrays.asList(new TempTable(UUID.randomUUID().toString(), "Blue", Boolean.TRUE),
                            new TempTable(UUID.randomUUID().toString(), "Red", Boolean.TRUE),
                            new TempTable(UUID.randomUUID().toString(), "Yellow", Boolean.FALSE)));
        } catch (Exception e) {
            log.error("Failed to load sample items. loadSampleItems - Exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public PagePublisher<TempTable3> getRecords() {
        return tempTableRepository4.findAll();
    }

    @Override
    public List<Key> deleteRecords(List<TempTable3> entities) {
        return tempTableRepository4.deleteAll(entities);
    }

    @Override
    public CompletableFuture<List<Key>> deleteAllAsync(List<TempTable3> entities) {
        return tempTableRepository4.deleteAllAsync(entities);
    }

    @Override
    public void createTable() {
        tempTableRepository4.createTable();
    }
}
