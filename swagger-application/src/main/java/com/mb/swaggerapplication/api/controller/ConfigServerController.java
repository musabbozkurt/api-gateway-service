package com.mb.swaggerapplication.api.controller;

import com.mb.swaggerapplication.config.ConfigServerProperties;
import com.mb.swaggerapplication.context.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
class ConfigServerController {

    private final ConfigServerProperties configServerProperties;

    @Value("${service.name:null}")
    private String serviceName;

    @Value("${service.first-url:null}")
    private String firstUrl;

    @GetMapping("/service-name")
    public String getServiceName() {
        log.info("Details: serviceName: {}, configServerName: {}, firstUrl: {}, configServerFirstUrl: {}, username: {}, clientId: {}",
                serviceName,
                configServerProperties.getName(),
                firstUrl,
                configServerProperties.getFirstUrl(),
                ContextHolder.getContext().username(),
                ContextHolder.getContext().clientId()
        );
        return serviceName + " - " +
                configServerProperties.getName() + " - " +
                firstUrl + " - " +
                configServerProperties.getFirstUrl() + " - " +
                ContextHolder.getContext().username() + " - " +
                ContextHolder.getContext().clientId();
    }
}
