package com.mb.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayGlobalFilters {

    private final ObjectMapper objectMapper;

    @Bean
    @Order(-1)
    public GlobalFilter a() {
        return (exchange, chain) -> {
            log.info("first pre filter. Path: {}", exchange.getRequest().getURI().getPath());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("third post filter")));
        };
    }

    @Bean
    @Order(0)
    public GlobalFilter b() {
        return (exchange, chain) -> {
            log.info("second pre filter. Path: {}", exchange.getRequest().getURI().getPath());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("second post filter")));
        };
    }

    @Bean
    @Order(1)
    public GlobalFilter c() {
        return (exchange, chain) -> {
            log.info("third pre filter. Path: {}", exchange.getRequest().getURI().getPath());
            new XSSRequestWrapper((HttpServletRequest) exchange.getRequest());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> log.info("first post filter")));
        };
    }

    // TODO more XSSRequestWrapper investigation should be done
    private String sanitiseResponse(InputStream responseDataStream) {
        if (Objects.nonNull(responseDataStream)) {
            try {
                return XSSRequestWrapper.cleanXss(objectMapper.writeValueAsString(responseDataStream));
            } catch (JsonProcessingException e) {
                log.error("Unable to sanitize response: {}", responseDataStream);
            }
        }
        return null;
    }
}
