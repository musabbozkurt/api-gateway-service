package com.mb.apigateway.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorHandlerConfiguration {

    @Bean
    public WebProperties webProperties() {
        return new WebProperties();
    }
    
    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties().getResources();
    }
}