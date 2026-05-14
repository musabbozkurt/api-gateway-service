package com.mb.stockexchangeservice.mapper;

import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.response.ApiStockResponse;
import com.mb.stockexchangeservice.base.BaseUnitTest;
import com.mb.stockexchangeservice.data.entity.Stock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockMapperTest extends BaseUnitTest {

    StockMapper stockMapper = Mappers.getMapper(StockMapper.class);

    @Test
    void map_StockToApiStockResponse_ShouldSucceed() {
        // arrange
        Stock stock = getStock();

        // act
        ApiStockResponse result = stockMapper.map(stock);

        // assertion
        assertEquals(stock.getName(), result.getName());
        assertEquals(stock.getDescription(), result.getDescription());
        assertEquals(stock.getCurrentPrice(), result.getCurrentPrice());
    }

    @Test
    void map_ListOfStockToListOfApiStockResponse_ShouldSucceed() {
        // arrange
        List<Stock> apiStockRequest = getStocks();

        // act
        List<ApiStockResponse> result = stockMapper.map(apiStockRequest);

        // assertion
        assertEquals(apiStockRequest.getFirst().getName(), result.getFirst().getName());
        assertEquals(apiStockRequest.getFirst().getDescription(), result.getFirst().getDescription());
        assertEquals(apiStockRequest.getFirst().getCurrentPrice(), result.getFirst().getCurrentPrice());
        assertEquals(apiStockRequest.getFirst().getId(), result.getFirst().getId());
    }

    @Test
    void map_ApiStockRequestToStock_ShouldSucceed() {
        // arrange
        ApiStockRequest apiStockRequest = getApiStockRequest();

        // act
        Stock result = stockMapper.map(apiStockRequest);

        // assertion
        assertEquals(apiStockRequest.getName(), result.getName());
        assertEquals(apiStockRequest.getDescription(), result.getDescription());
        assertEquals(apiStockRequest.getCurrentPrice(), result.getCurrentPrice());
    }
}
