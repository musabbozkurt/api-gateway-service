package com.mb.brokerageprovider.service;

import com.mb.brokerageprovider.data.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {

    Page<Stock> getAllStocks(Pageable pageable);

    Stock getStockById(Long stockId);

    Stock createStock(Stock stock);

    Stock updateStockById(Long stockId, Stock stock);

    void deleteStockById(Long stockId);

    Boolean checkStockAvailability(Long stockId);

    Stock getStockByProductCode(String productCode);
}
