package com.mb.brokerageprovider.service.impl;

import com.mb.brokerageprovider.data.entity.Stock;
import com.mb.brokerageprovider.data.repository.StockRepository;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .orElseThrow(() -> new BaseException(BrokerageProviderErrorCode.STOCK_NOT_FOUND));
    }

    @Override
    public Stock createStock(Stock stock) {
        return stockRepository.save(stock);
    }

    @Override
    public Stock updateStockById(Long stockId, Stock newStock) {
        Stock existingStock = getStockById(stockId);
        existingStock.setProductCode(newStock.getProductCode());
        existingStock.setQuantity(newStock.getQuantity());
        stockRepository.save(existingStock);
        return existingStock;
    }

    @Override
    public void deleteStockById(Long stockId) {
        stockRepository.deleteById(getStockById(stockId).getId());
    }

    @Override
    public Boolean checkStockAvailability(Long stockId) {
        return getStockById(stockId).getQuantity().compareTo(0L) > 0;
    }

    @Override
    public Stock getStockByProductCode(String productCode) {
        return stockRepository.findByProductCode(productCode)
                .orElseThrow(() -> new BaseException(BrokerageProviderErrorCode.STOCK_NOT_FOUND));
    }
}
