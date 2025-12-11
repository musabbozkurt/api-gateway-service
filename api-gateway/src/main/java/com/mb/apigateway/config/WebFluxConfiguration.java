package com.mb.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.http.codec.autoconfigure.HttpCodecsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfiguration implements WebFluxConfigurer {

    private final HttpCodecsProperties httpCodecsProperties;

    @Override
    public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
        DataSize maxInMemorySize = httpCodecsProperties.getMaxInMemorySize();
        if (maxInMemorySize != null) {
            configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(maxInMemorySize.toBytes()));
        }
    }

    @Bean
    public RouterFunction<ServerResponse> redirectToSwagger() {
        return route(GET("/"), _ -> ServerResponse.permanentRedirect(URI.create("/swagger-ui.html")).build());
    }
}
