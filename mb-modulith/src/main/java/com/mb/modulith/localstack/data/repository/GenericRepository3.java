package com.mb.modulith.localstack.data.repository;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.List;

/**
 * GenericRepository
 *
 * @param <T></T> is for TYPE
 */
public interface GenericRepository3<T> {

    PageIterable<T> findAll();

    List<Key> deleteAll(List<T> entities);
}
