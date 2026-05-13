package com.mb.stockexchangeservice.mapper;

import com.mb.stockexchangeservice.api.request.ApiStockExchangeRequest;
import com.mb.stockexchangeservice.api.response.ApiStockExchangeResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.data.entity.StockExchange;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockExchangeMapperTest extends BaseUnitTest {

    StockExchangeMapper stockExchangeMapper = Mappers.getMapper(StockExchangeMapper.class);

    @Test
    void map_StockExchangeToApiStockExchangeResponse_ShouldSucceed() {
        // arrange
        StockExchange stockExchange = getStockExchange();

        // act
        ApiStockExchangeResponse result = stockExchangeMapper.map(stockExchange);

        // assertion
        assertEquals(stockExchange.getName(), result.getName());
        assertEquals(stockExchange.getDescription(), result.getDescription());
        assertEquals(stockExchange.isLiveInMarket(), result.isLiveInMarket());
        assertEquals(stockExchange.getStocks().getFirst().getName(), result.getStocks().getFirst().getName());
        assertEquals(stockExchange.getStocks().getFirst().getDescription(), result.getStocks().getFirst().getDescription());
        assertEquals(stockExchange.getStocks().getFirst().getCurrentPrice(), result.getStocks().getFirst().getCurrentPrice());
    }

    @Test
    void map_ApiStockExchangeRequestToStockExchange_ShouldSucceed() {
        // arrange
        ApiStockExchangeRequest apiStockExchangeRequest = getApiStockExchangeRequest();

        // act
        StockExchange result = stockExchangeMapper.map(apiStockExchangeRequest);

        // assertion
        assertEquals(apiStockExchangeRequest.getName(), result.getName());
        assertEquals(apiStockExchangeRequest.getDescription(), result.getDescription());
    }
}
