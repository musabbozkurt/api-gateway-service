/**
 * AuthenticationFilter.java
 * <p>
 * This filter extracts user information from the JWT token and adds it to the request headers.
 * It implements GlobalFilter and Ordered interfaces to ensure it runs for every request
 * and has the highest precedence.
 * <p>
 * Key functionalities:
 * - Extracts user ID, username, and client ID from the JWT token.
 * - Adds these details to the request headers for downstream services.
 * - Handles cases where token attributes might be missing by decoding the JWT manually.
 * <p>
 * Dependencies:
 * - Spring Cloud Gateway for filtering requests.
 * - Spring Security for accessing security context and authentication details.
 * - Jackson ObjectMapper for JSON processing.
 * <p>
 * Note: Ensure that the necessary constants are defined in GatewayServiceConstants.

package com.mb.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(requestBuilder -> requestBuilder.headers(headers -> setHeaders(exchange, securityContext, headers)))
                            .build();
                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void setHeaders(ServerWebExchange exchange, SecurityContext securityContext, HttpHeaders headers) {
        String userId = null;
        String username = null;
        String clientId = null;

        if (securityContext.getAuthentication() instanceof BearerTokenAuthentication bearerAuth) {
            Map<String, Object> tokenAttributes = bearerAuth.getTokenAttributes();

            userId = Optional.ofNullable(tokenAttributes.get(USER_ID))
                    .map(Object::toString)
                    .orElse(null);

            username = Optional.ofNullable(tokenAttributes.get(USER_NAME))
                    .map(Object::toString)
                    .orElse(null);

            clientId = Optional.ofNullable(tokenAttributes.get(CLIENT_ID))
                    .map(Object::toString)
                    .orElse(null);

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(username) || StringUtils.isBlank(clientId)) {
                HashMap<String, String> claims = extractJwtClaims(bearerAuth.getToken().getTokenValue());
                userId = StringUtils.isNotBlank(userId) ? userId : String.valueOf(claims.get(USER_ID));
                username = StringUtils.isNotBlank(username) ? username : claims.get(USER_NAME);
                clientId = StringUtils.isNotBlank(clientId) ? clientId : claims.get(CLIENT_ID);
            }
        }

        headers.set(USERNAME, username);
        headers.set(USER_ID, userId);
        headers.set(CLIENT_ID, clientId);

        exchange.getAttributes().put(USERNAME, username);
        exchange.getAttributes().put(CLIENT_ID, clientId);
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
*/
