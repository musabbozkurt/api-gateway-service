package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.config.StockExchangeProperties;
import com.mb.stockexchangeservice.data.entity.Stock;
import com.mb.stockexchangeservice.data.entity.StockExchange;
import com.mb.stockexchangeservice.data.repository.StockExchangeRepository;
import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.service.StockExchangeService;
import com.mb.stockexchangeservice.service.StockService;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockExchangeServiceImpl implements StockExchangeService {

    private final StockExchangeRepository stockExchangeRepository;
    private final StockService stockService;
    private final StockExchangeProperties stockExchangeProperties;

    @Override
    public StockExchange createStockExchange(StockExchange stockExchange) {
        return stockExchangeRepository.save(stockExchange);
    }

    @Override
    public StockExchange findByName(String name) {
        return stockExchangeRepository.findByName(name).orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.STOCK_EXCHANGE_NOT_FOUND));
    }

    @Override
    @Transactional
    public StockExchange addStockToStockExchange(String name, Stock stock) {
        StockExchange stockExchange = this.findByName(name);
        stockExchange.addStock(stockService.getStockByName(stock.getName()));
        stockExchange.setLiveInMarket(isLiveInMarket(stockExchange));
        try {
            return stockExchangeRepository.save(stockExchange);
        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic locking failure occurred while saving stock exchange. addStockToStockExchange - Exception: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.OPTIMISTIC_LOCKING_FAILURE);
        }
    }

    @Override
    @Transactional
    public StockExchange deleteStockFromStockExchange(String name, String stockName) {
        try {
            StockExchange stockExchange = stockExchangeRepository.findStockExchangeByName(name).orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.STOCK_EXCHANGE_NOT_FOUND));
            stockExchange.removeStock(stockService.getStockByName(stockName));
            stockExchange.setLiveInMarket(isLiveInMarket(stockExchange));
            return stockExchangeRepository.save(stockExchange);
        } catch (PessimisticLockException e) {
            log.error("Pessimistic lock exception occurred while deleting stock from stock exchange. deleteStockFromStockExchange - Exception: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.PESSIMISTIC_LOCKING_FAILURE);
        }
    }

    private boolean isLiveInMarket(StockExchange stockExchange) {
        return stockExchange.getStocks().size() >= stockExchangeProperties.getMinimumNumberOfStocks();
    }
}
