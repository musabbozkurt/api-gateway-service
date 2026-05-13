package com.mb.stockexchangeservice.service;

import com.mb.stockexchangeservice.data.entity.Stock;
import com.mb.stockexchangeservice.data.entity.StockExchange;

public interface StockExchangeService {

    StockExchange createStockExchange(StockExchange stockExchange);

    StockExchange findByName(String name);

    StockExchange addStockToStockExchange(String name, Stock stock);

    StockExchange deleteStockFromStockExchange(String name, String stockName);
}
