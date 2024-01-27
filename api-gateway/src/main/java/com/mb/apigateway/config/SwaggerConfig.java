package com.mb.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerConfigProperties swaggerConfigProperties;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        swaggerConfigProperties
                .getUrls()
                .forEach(swaggerUIUrl -> routes
                        .route(r -> r.path(swaggerUIUrl.getUrl()).and().method(HttpMethod.GET).uri(swaggerUIUrl.getUri())));

        return routes.build();
    }

}