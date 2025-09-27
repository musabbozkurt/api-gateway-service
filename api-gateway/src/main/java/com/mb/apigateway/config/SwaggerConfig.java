package com.mb.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

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
                        .route(r -> r.path(swaggerUIUrl.getUrl())
                                .and().method(HttpMethod.GET)
                                .filters(f -> f.modifyResponseBody(String.class, String.class, (exchange, body) -> Mono.just(injectServerUrl(body, swaggerUIUrl.getServerUrl()))))
                                .uri(swaggerUIUrl.getUri())));

        return routes.build();
    }

    private String injectServerUrl(String body, String serverUrl) {
        if (StringUtils.isBlank(body) || !body.contains("\"openapi\"")) {
            return body;
        }

        // Check if servers already exists
        if (body.contains("\"servers\"")) {
            // Replace existing servers section
            return body.replaceFirst("\"servers\":\\s*\\[[^]]*]",
                    String.format("\"servers\":[{\"url\":\"%s\"}]", serverUrl));
        } else {
            // Add new servers section after openapi field
            String serverJson = String.format("\"servers\":[{\"url\":\"%s\"}],", serverUrl);
            return body.replaceFirst("(\"openapi\":\"[^\"]+\",)", "$1" + serverJson);
        }
    }
}
