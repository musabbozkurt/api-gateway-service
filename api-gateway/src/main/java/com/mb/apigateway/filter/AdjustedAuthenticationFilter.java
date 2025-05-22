package com.mb.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustedAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String USERNAME = "username";
    private static final String USER_NAME = "user_name";
    private static final String USER_ID = "userId";

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(requestBuilder -> requestBuilder.headers(headers -> setHeaders(exchange, securityContext, headers)))
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .doFinally(signal -> MDC.clear());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private void setHeaders(ServerWebExchange exchange, SecurityContext securityContext, HttpHeaders headers) {
        String userId = null;
        String username = null;

        if (securityContext.getAuthentication() instanceof BearerTokenAuthentication bearerAuth) {
            Map<String, Object> tokenAttributes = bearerAuth.getTokenAttributes();

            userId = Optional.ofNullable(tokenAttributes.get(USER_ID))
                    .map(Object::toString)
                    .orElse(null);

            username = Optional.ofNullable(tokenAttributes.get(USER_NAME))
                    .map(Object::toString)
                    .orElse(null);

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(username)) {
                HashMap<String, String> claims = extractJwtClaims(bearerAuth.getToken().getTokenValue());
                userId = StringUtils.isNotBlank(userId) ? userId : String.valueOf(claims.get(USER_ID));
                username = StringUtils.isNotBlank(username) ? username : claims.get(USER_NAME);
            }
        }

        headers.set(USERNAME, StringUtils.isNotBlank(username) ? username : exchange.getRequest().getHeaders().getFirst(USERNAME));
        headers.set(USER_ID, StringUtils.isNotBlank(userId) ? userId : exchange.getRequest().getHeaders().getFirst(USER_ID));

        MDC.put(USERNAME, username);
    }

    private HashMap<String, String> extractJwtClaims(String token) {
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String[] chunks = token.split("\\.");
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payload, HashMap.class);
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
