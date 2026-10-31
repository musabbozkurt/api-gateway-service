package com.mb.modulith.commerce.data.repository;

import com.mb.modulith.commerce.data.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, QuerydslPredicateExecutor<Order> {

    Optional<Order> findByIdAndDeletedIsFalse(UUID uuid);
}
