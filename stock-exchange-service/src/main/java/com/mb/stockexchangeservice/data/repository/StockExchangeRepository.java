package com.mb.stockexchangeservice.data.repository;

import com.mb.stockexchangeservice.data.entity.StockExchange;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockExchangeRepository extends JpaRepository<StockExchange, Long> {

    Optional<StockExchange> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockExchange> findStockExchangeByName(String name);
}
