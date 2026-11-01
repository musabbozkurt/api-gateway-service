package com.mb.modulith.localstack.data.repository;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T, I> {

    List<T> findAll();

    Optional<T> findById(I id);

    T save(T entity);

    void delete(T entity);

    void deleteAll(List<T> entities);

    List<T> findAllWithLimit(int limit);
}
