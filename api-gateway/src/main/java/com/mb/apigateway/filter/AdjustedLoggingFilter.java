package com.mb.apigateway.filter;

import com.mb.apigateway.context.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.MDC_CONTEXT;
import static com.mb.apigateway.constant.GatewayServiceConstants.SESSION_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustedLoggingFilter implements GlobalFilter, Ordered {

    private static final HashSet<MediaType> SUPPORTED_MEDIA_TYPES = Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED).collect(Collectors.toCollection(HashSet::new));
    private static final String UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED = "Unsupported Media Type Not Logged";

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        AtomicReference<String> sessionId = new AtomicReference<>();

        return exchange.getSession()
                .doOnNext(webSession -> {
                    sessionId.set(webSession.getId());
                    initializeMDCAndContext(exchange, sessionId.get());
                    log.info("Incoming request. Method: {}, URI: {}", request.getMethod(), request.getURI());
                    // Store complete MDC context in exchange attributes
                    Map<String, String> mdcContext = MDC.getCopyOfContextMap();
                    exchange.getAttributes().put(MDC_CONTEXT, mdcContext);
                })
                .then(
                        chain.filter(exchange)
                                .doOnError(error ->
                                        executeWithMDC(exchange, () -> log.error("Request processing failed. Method: {}, URI: {}, RequestBody: {}, Error: {}",
                                                        request.getMethod(),
                                                        request.getURI(),
                                                        SUPPORTED_MEDIA_TYPES.contains(request.getHeaders().getContentType()) ? exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR) : UNSUPPORTED_MEDIA_TYPE_NOT_LOGGED,
                                                        error.getMessage()
                                                ),
                                                sessionId.get()
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
                                                                ),
                                                                sessionId.get()
                                                        );
                                                    }
                                                }
                                        )
                                )
                );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private void initializeMDCAndContext(ServerWebExchange exchange, String sessionId) {
        String username = (String) exchange.getAttributes().get(USERNAME);
        String clientId = (String) exchange.getAttributes().get(CLIENT_ID);

        ContextHolder.Context.ContextBuilder contextBuilder = ContextHolder.Context.builder();

        if (StringUtils.isNotBlank(username)) {
            MDC.put(USERNAME, username);
            contextBuilder.username(username);
        }

        if (StringUtils.isNotBlank(clientId)) {
            MDC.put(CLIENT_ID, clientId);
            contextBuilder.clientId(clientId);
        }

        exchange.mutate()
                .request(requestBuilder -> requestBuilder.headers(headers -> headers.set(SESSION_ID, sessionId)))
                .build();
        MDC.put(SESSION_ID, sessionId);
        contextBuilder.sessionId(sessionId);

        ContextHolder.setContext(contextBuilder.build());
    }

    // Ensure MDC is properly managed in reactive context
    private void executeWithMDC(ServerWebExchange exchange, Runnable operation, String sessionId) {
        Map<String, String> previousMDC = MDC.getCopyOfContextMap();
        try {
            initializeMDCAndContext(exchange, sessionId);
            operation.run();
        } finally {
            if (previousMDC != null) {
                MDC.setContextMap(previousMDC);
            }
            // Remove cleanup - GatewayGlobalFilters handle clearing
        }
    }
}
