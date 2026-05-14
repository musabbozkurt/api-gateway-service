package com.mb.stockexchangeservice.base;

import com.mb.stockexchangeservice.api.request.ApiStockExchangeRequest;
import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.request.ApiUserRequest;
import com.mb.stockexchangeservice.data.entity.Stock;
import com.mb.stockexchangeservice.data.entity.StockExchange;
import com.mb.stockexchangeservice.data.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public abstract class BaseUnitTest {

    public static User getUser() {
        User user = new User();
        user.setId(2L);
        user.setCreatedDateTime(OffsetDateTime.now());
        user.setModifiedDateTime(OffsetDateTime.now());
        user.setFirstName("Jack");
        user.setLastName("Hack");
        user.setUsername("jack_hack");
        user.setEmail("jack.hack@gmail.com");
        user.setPhoneNumber("1234567890");
        return user;
    }

    public static ApiUserRequest getApiUserRequest() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setFirstName("Jack");
        apiUserRequest.setLastName("Hack");
        apiUserRequest.setUsername("jack_hack");
        apiUserRequest.setPassword("test1234");
        apiUserRequest.setEmail("jack.hack@gmail.com");
        apiUserRequest.setPhoneNumber("1234567899");
        return apiUserRequest;
    }

    public static ApiUserRequest getApiUserRequest2() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setFirstName("Jack");
        apiUserRequest.setLastName("Hack");
        apiUserRequest.setUsername("jack_hack");
        apiUserRequest.setPassword("test1234");
        apiUserRequest.setEmail("jack.hack@gmail.com");
        apiUserRequest.setPhoneNumber("1234567890");
        return apiUserRequest;
    }

    public static ApiUserRequest getApiUserRequest3() {
        ApiUserRequest apiUserRequest = new ApiUserRequest();
        apiUserRequest.setEmail("jack.hack.new@gmail.com");
        return apiUserRequest;
    }

    public static Stock getStock() {
        Stock stock = new Stock();
        stock.setName("APPL");
        stock.setDescription("APPLE");
        stock.setCurrentPrice(BigDecimal.valueOf(22.12));
        return stock;
    }

    public static List<Stock> getStocks() {
        return List.of(getStock());
    }

    public static ApiStockRequest getApiStockRequest() {
        return ApiStockRequest.builder().name("MSFT").description("MICROSOFT").currentPrice(BigDecimal.valueOf(322.12)).build();
    }

    public static ApiStockRequest getApiStockRequest2() {
        return ApiStockRequest.builder().name("AAPL").description("APPLE").currentPrice(BigDecimal.valueOf(22.12)).build();
    }

    public static ApiStockExchangeRequest getApiStockExchangeRequest() {
        ApiStockExchangeRequest apiStockExchangeRequest = new ApiStockExchangeRequest();
        apiStockExchangeRequest.setName("Hong Kong Stock Exchange");
        apiStockExchangeRequest.setDescription("Hong Kong Stock Exchange Description");
        return apiStockExchangeRequest;
    }

    public static StockExchange getStockExchange() {
        StockExchange stockExchange = new StockExchange();
        stockExchange.setName("New York Stock Exchange");
        stockExchange.setDescription("New York Stock Exchange Description");
        stockExchange.setLiveInMarket(false);
        stockExchange.setStocks(List.of(getStock()));
        return stockExchange;
    }
}
