package com.mb.modulith.localstack.data.repository;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GenericRepository
 *
 * @param <T></T> is for TYPE
 */
public interface GenericRepository4<T> {

    PagePublisher<T> findAll();

    List<Key> deleteAll(List<T> entities);

    CompletableFuture<List<Key>> deleteAllAsync(List<T> entities);

    void createTable();
}
