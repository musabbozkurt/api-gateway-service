package com.mb.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "springdoc.swagger-ui")
public class SwaggerConfigProperties {

    private List<SwaggerUIUrl> urls;

    @Data
    public static class SwaggerUIUrl {
        private String url;
        private String name;
        private String uri;
    }
}
