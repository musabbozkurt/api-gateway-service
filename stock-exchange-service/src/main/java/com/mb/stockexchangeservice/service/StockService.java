package com.mb.stockexchangeservice.service;

import com.mb.stockexchangeservice.data.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {

    Page<Stock> getAllStocks(Pageable pageable);

    Stock getStockById(Long stockId);

    Stock createStock(Stock stock);

    Stock updateStockById(Long stockId, Stock stock);

    void deleteStockById(Long stockId);

    Stock getStockByName(String name);
}
