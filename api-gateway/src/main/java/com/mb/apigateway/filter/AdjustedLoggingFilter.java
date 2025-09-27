package com.mb.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.DEFAULT_VALUE;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustedLoggingFilter implements GlobalFilter, Ordered {

    private static final HashSet<MediaType> SUPPORTED_MEDIA_TYPES = Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED).collect(Collectors.toCollection(HashSet::new));
    private static final String UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED = "Unsupported Media Type Not Logged";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MDC.put(USERNAME, (String) exchange.getAttributes().getOrDefault(USERNAME, DEFAULT_VALUE));
        MDC.put(CLIENT_ID, (String) exchange.getAttributes().getOrDefault(CLIENT_ID, DEFAULT_VALUE));

        return chain.filter(exchange)
                .doOnError(error ->
                        executeWithMDC(exchange, () -> log.error("Request processing failed. Method: {}, URI: {}, RequestBody: {}, Error: {}",
                                        request.getMethod(),
                                        request.getURI(),
                                        SUPPORTED_MEDIA_TYPES.contains(request.getHeaders().getContentType()) ? exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR) : UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED,
                                        error.getMessage()
                                )
                        )
                )
                .then(Mono.fromRunnable(() -> {
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    if (statusCode != null && statusCode.isError()) {
                        executeWithMDC(exchange, () -> log.error("Request processing failed. Method: {}, URI: {}, RequestBody: {}, Status: {}",
                                        request.getMethod(),
                                        request.getURI(),
                                        SUPPORTED_MEDIA_TYPES.contains(request.getHeaders().getContentType()) ? exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR) : UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED,
                                        statusCode
                                )
                        );
                    }
                }));
    }

    // Ensure MDC is properly managed in reactive context
    private void executeWithMDC(ServerWebExchange exchange, Runnable operation) {
        Map<String, String> previousMDC = MDC.getCopyOfContextMap();
        try {
            populateMDC(exchange);
            operation.run();
        } finally {
            if (previousMDC != null) {
                MDC.setContextMap(previousMDC);
            } else {
                MDC.clear();
            }
        }
    }

    private void populateMDC(ServerWebExchange exchange) {
        MDC.put(USERNAME, (String) exchange.getAttributes().getOrDefault(USERNAME, DEFAULT_VALUE));
        MDC.put(CLIENT_ID, (String) exchange.getAttributes().getOrDefault(CLIENT_ID, DEFAULT_VALUE));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
