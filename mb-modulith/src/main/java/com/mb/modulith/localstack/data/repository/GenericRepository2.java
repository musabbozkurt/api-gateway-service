package com.mb.modulith.localstack.data.repository;

import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.List;

/**
 * GenericRepository
 *
 * @param <T></T> is for TYPE
 */
public interface GenericRepository2<T> {

    Iterable<T> findAll();

    void deleteAll(List<T> entities);

    GetItemResponse getItem(GetItemRequest itemRequest);

    void createTableIfNotExists(Class<?> clazz);

    void loadSampleItems(String tableName, List<T> items);
}
