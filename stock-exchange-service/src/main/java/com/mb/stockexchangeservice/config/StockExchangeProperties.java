package com.mb.stockexchangeservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stock-exchange")
public class StockExchangeProperties {

    private int minimumNumberOfStocks;
}
