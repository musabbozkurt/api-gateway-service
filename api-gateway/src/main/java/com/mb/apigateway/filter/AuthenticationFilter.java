package com.mb.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String USERNAME = "username";
    private static final String USER_NAME = "user_name";
    private static final String USER_ID = "userId";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r.headers(headers -> setHeaders(exchange, securityContext, headers)))
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .doFinally(signal -> MDC.clear());
    }

    @Override
    public int getOrder() {
        // Set a high precedence to ensure this filter runs first
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void setHeaders(ServerWebExchange exchange, SecurityContext securityContext, HttpHeaders headers) {
        String userId = null;
        String username = null;
        String token = null;

        // Try to extract userId, username and token from Bearer token
        if (securityContext.getAuthentication() instanceof BearerTokenAuthentication bearerAuth) {
            token = bearerAuth.getToken().getTokenValue();

            // Extract userId from token attributes
            if (bearerAuth.getTokenAttributes().containsKey(USER_ID)) {
                userId = bearerAuth.getTokenAttributes().get(USER_ID).toString();
                log.debug("Extracted userId from token. setHeaders - userId: {}", userId);
            }

            // Extract username from token attributes
            if (bearerAuth.getTokenAttributes().containsKey(USER_NAME)) {
                username = bearerAuth.getTokenAttributes().get(USER_NAME).toString();
                log.debug("Extracted username from token. setHeaders - username: {}", username);
            }
        }

        headers.set(USERNAME, username != null ? username : exchange.getRequest().getHeaders().getFirst(USERNAME));
        headers.set(USER_ID, userId != null ? userId : exchange.getRequest().getHeaders().getFirst(USER_ID));
        headers.set(AUTHORIZATION, "%s%s".formatted(BEARER, token != null ? token : securityContext.getAuthentication().getCredentials().toString()));

        MDC.put(USERNAME, username);
    }
}
