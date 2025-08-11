package com.mb.swaggerapplication.api.controller;

import com.mb.swaggerapplication.config.ConfigServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return serviceName + " - " + configServerProperties.getName() + " - " + firstUrl + " - " + configServerProperties.getFirstUrl();
    }
}
