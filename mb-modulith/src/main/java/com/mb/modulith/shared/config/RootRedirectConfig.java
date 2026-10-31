package com.mb.modulith.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RootRedirectConfig implements WebMvcConfigurer {

    private static final String SWAGGER_UI_INDEX = "/swagger-ui/index.html";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", SWAGGER_UI_INDEX);
    }
}
