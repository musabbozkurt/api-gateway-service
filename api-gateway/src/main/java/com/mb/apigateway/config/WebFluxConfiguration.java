package com.mb.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfiguration implements WebFluxConfigurer {

    private final CodecProperties codecProperties;

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(codecProperties.getMaxInMemorySize().toBytes()));
    }
}
