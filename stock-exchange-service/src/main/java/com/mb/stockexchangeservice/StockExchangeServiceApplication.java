package com.mb.stockexchangeservice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class StockExchangeServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(StockExchangeServiceApplication.class, args);
    }
}
