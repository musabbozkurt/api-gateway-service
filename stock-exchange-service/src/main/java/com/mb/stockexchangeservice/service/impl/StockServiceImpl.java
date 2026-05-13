package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.data.entity.Stock;
import com.mb.stockexchangeservice.data.repository.StockRepository;
import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public Page<Stock> getAllStocks(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    @Override
    public Stock getStockById(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.STOCK_NOT_FOUND));
    }

    @Override
    public Stock createStock(Stock stock) {
        stockRepository.findByName(stock.getName()).ifPresent(_ -> {
            throw new BaseException(StockExchangeServiceErrorCode.ALREADY_EXISTS);
        });
        return stockRepository.save(stock);
    }

    @Override
    public Stock updateStockById(Long stockId, Stock newStock) {
        Stock existingStock = getStockById(stockId);
        boolean shouldBeUpdated = false;
        if (!Strings.CI.equals(existingStock.getName(), newStock.getName())) {
            existingStock.setName(newStock.getName());
            shouldBeUpdated = true;
        }
        if (!Strings.CI.equals(existingStock.getDescription(), newStock.getDescription())) {
            existingStock.setDescription(newStock.getDescription());
            shouldBeUpdated = true;
        }
        if (ObjectUtils.notEqual(existingStock.getCurrentPrice(), newStock.getCurrentPrice())) {
            existingStock.setCurrentPrice(newStock.getCurrentPrice());
            shouldBeUpdated = true;
        }
        if (shouldBeUpdated) {
            return stockRepository.save(existingStock);
        }
        return existingStock;
    }

    @Override
    public void deleteStockById(Long stockId) {
        stockRepository.deleteById(getStockById(stockId).getId());
    }

    @Override
    public Stock getStockByName(String name) {
        return stockRepository.findByName(name)
                .orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.STOCK_NOT_FOUND));
    }
}
