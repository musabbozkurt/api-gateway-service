package com.mb.gitlabservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitlabSpringDocConfig {

    @Bean
    public GroupedOpenApi gitlabOpenApi() {
        return GroupedOpenApi.builder()
                .group("gitlab")
                .packagesToScan("com.mb.gitlabservice.api")
                .pathsToMatch("/gitlab/**")
                .build();
    }
}
