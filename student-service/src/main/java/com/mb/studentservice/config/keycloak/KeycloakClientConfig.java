package com.mb.studentservice.config.keycloak;

import feign.Logger;
import feign.slf4j.Slf4jLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class KeycloakClientConfig {

    @Value("${feign.services.keycloak.log.level}")
    private Logger.Level logLevel;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return logLevel;
    }

    @Bean
    public Logger keycloakClientLogger() {
        return new Slf4jLogger(KeycloakClientConfig.class);
    }

}
