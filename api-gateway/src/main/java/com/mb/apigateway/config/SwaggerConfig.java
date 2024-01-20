package com.mb.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "swagger.documentation")
public class SwaggerConfig {

    List<SwaggerServices> services;

    @Getter
    @Setter
    @ToString
    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "swagger.documentation.services")
    public static class SwaggerServices {
        private String name;
        private String url;
        private String version;
    }
}
