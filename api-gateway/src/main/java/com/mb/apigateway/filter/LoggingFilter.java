package com.mb.apigateway.filter;

import com.mb.apigateway.config.LoggingConfig;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String TEST_USERNAME = "test_username";
    private static final String TEST_CLIENT_ID = "test_client_id";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final LoggingConfig loggingConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip logging if disabled or path is excluded
        if (!loggingConfig.isEnabled() || isPathExcluded(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();

        exchange.mutate()
                .request(requestBuilder -> requestBuilder.headers(headers -> {
                    headers.set(USERNAME, TEST_USERNAME);
                    headers.set(CLIENT_ID, TEST_CLIENT_ID);
                }))
                .build();

        exchange.getAttributes().put(USERNAME, TEST_USERNAME);
        exchange.getAttributes().put(CLIENT_ID, TEST_CLIENT_ID);

        // This class can store MDC context because it runs before other filters

        // Log the request details
        log.info("Request: [{}] {} {} from {}", requestId, request.getMethod(), request.getURI(), request.getRemoteAddress());

        // Log request headers
        request.getHeaders().forEach((name, values) -> values.forEach(value -> log.debug("Request Header: [{}] {}: {}", requestId, name, value)));

        // Record the start time
        long startTime = System.currentTimeMillis();

        // Add a filter to log the response details
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    log.info("Response: [{}] Status: {} - Completed in {} ms", requestId, exchange.getResponse().getStatusCode(), duration);

                    // Log response headers
                    exchange.getResponse().getHeaders().forEach((name, values) -> values.forEach(value -> log.debug("Response Header: [{}] {}: {}", requestId, name, value)));
                }));
    }

    @Override
    public int getOrder() {
        // Set a high precedence to ensure this filter runs first
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPathExcluded(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }

        // Check if path ends with excluded file extension
        String extension = getFileExtension(path);
        if (StringUtils.isNotBlank(extension) && loggingConfig.getExcludeFileExtensions().contains(extension.toLowerCase())) {
            return true;
        }

        // Check if path matches any excluded patterns
        return loggingConfig.getExcludePaths()
                .stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1);
        }
        return "";
    }
}
