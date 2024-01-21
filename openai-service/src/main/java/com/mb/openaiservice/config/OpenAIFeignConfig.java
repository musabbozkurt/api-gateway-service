package com.mb.openaiservice.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class OpenAIFeignConfig {

    private final OpenAIConfigProperties openAIConfigProperties;

    @Bean
    public RequestInterceptor openaiFeignRequestInterceptor() {
        return new OpenAIClientRequestInterceptor(openAIConfigProperties);
    }

    @Bean
    public ErrorDecoder openaiErrorDecoder() {
        return new OpenAIErrorDecoder();
    }
}