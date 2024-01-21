package com.mb.openaiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feign.services.openai-client")
public class OpenAIConfigProperties {
    private String url;
    private String token;
}