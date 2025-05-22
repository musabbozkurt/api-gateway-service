package com.mb.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustedLoggingFilter implements GlobalFilter, Ordered {

    private static final HashSet<MediaType> SUPPORTED_MEDIA_TYPES = Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED).collect(Collectors.toCollection(HashSet::new));
    private static final String UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED = "Unsupported Media Type Not Logged";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        return chain.filter(exchange)
                .doOnError(error -> log.error("Request processing failed. Method: {}, URI: {}, RequestBody: {}, Error: {}",
                                request.getMethod(),
                                request.getURI(),
                                SUPPORTED_MEDIA_TYPES.contains(request.getHeaders().getContentType()) ? exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR) : UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED,
                                error.getMessage()
                        )
                )
                .then(Mono.fromRunnable(() -> {
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    if (statusCode != null && statusCode.isError()) {
                        log.error("Request processing failed. Method: {}, URI: {}, RequestBody: {}, Status: {}",
                                request.getMethod(),
                                request.getURI(),
                                SUPPORTED_MEDIA_TYPES.contains(request.getHeaders().getContentType()) ? exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR) : UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED,
                                statusCode
                        );
                    }
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
