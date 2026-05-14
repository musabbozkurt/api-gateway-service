package com.mb.stockexchangeservice.mapper;

import com.mb.stockexchangeservice.api.request.ApiStockExchangeRequest;
import com.mb.stockexchangeservice.api.response.ApiStockExchangeResponse;
import com.mb.stockexchangeservice.data.entity.StockExchange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockExchangeMapper {

    ApiStockExchangeResponse map(StockExchange stockExchange);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    @Mapping(target = "liveInMarket", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    StockExchange map(ApiStockExchangeRequest apiStockExchangeRequest);
}
