package com.mb.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "user-activity.elasticsearch")
public class UserActivityElasticsearchProperties {

    private String baseUrl = "http://localhost:9200";
    private String username;
    private String password;
    private boolean enabled = true;
    private String indexPrefix = "user-activity";
}
