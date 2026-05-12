package com.mb.brokerageprovider.mapper;

import com.mb.brokerageprovider.api.request.ApiStockRequest;
import com.mb.brokerageprovider.api.response.ApiStockResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.data.entity.Stock;
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
        assertEquals(stock.getQuantity(), result.getQuantity());
        assertEquals(stock.getProductCode(), result.getProductCode());
    }

    @Test
    void map_ListOfStockToListOfApiStockResponse_ShouldSucceed() {
        // arrange
        List<Stock> apiStockRequest = getStocks();

        // act
        List<ApiStockResponse> result = stockMapper.map(apiStockRequest);

        // assertion
        assertEquals(apiStockRequest.getFirst().getQuantity(), result.getFirst().getQuantity());
        assertEquals(apiStockRequest.getFirst().getProductCode(), result.getFirst().getProductCode());
        assertEquals(apiStockRequest.getFirst().getId(), result.getFirst().getId());
    }

    @Test
    void map_ApiStockRequestToStock_ShouldSucceed() {
        // arrange
        ApiStockRequest apiStockRequest = getApiStockRequest();

        // act
        Stock result = stockMapper.map(apiStockRequest);

        // assertion
        assertEquals(apiStockRequest.getQuantity(), result.getQuantity());
        assertEquals(apiStockRequest.getProductCode(), result.getProductCode());
    }
}
