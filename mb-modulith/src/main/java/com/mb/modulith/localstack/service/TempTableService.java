package com.mb.modulith.localstack.service;

import com.mb.modulith.localstack.data.model.TempTable;
import com.mb.modulith.localstack.data.model.TempTable3;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TempTableService {

    Iterable<TempTable> getTempTableRecords();

    void deleteTempTableRecords(List<TempTable> entities);

    GetItemResponse getItem(String id);

    void createTableIfNotExists();

    void loadSampleItems();

    PagePublisher<TempTable3> getRecords();

    List<Key> deleteRecords(List<TempTable3> entities);

    CompletableFuture<List<Key>> deleteAllAsync(List<TempTable3> entities);

    void createTable();
}
