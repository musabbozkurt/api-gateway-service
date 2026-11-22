package com.mb.studentservice.config.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestClient getRestTemplate() {
        return RestClient.builder().build();
    }

}
