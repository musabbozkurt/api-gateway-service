package com.mb.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
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
import java.util.Objects;
import java.util.Optional;

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_NAME;

/**
 * Propagates authenticated user identity to downstream services via request headers.
 * <p>
 * For authenticated requests, extracts {@code userId}, {@code username}, and {@code clientId}
 * from the {@link org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication}
 * token attributes — falling back to manual JWT payload decoding when introspection omits those claims.
 * Unauthenticated requests (permitted paths) are passed through unchanged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(optionalSecurityContext -> {
                    if (optionalSecurityContext.isEmpty()) {
                        return chain.filter(exchange);
                    }
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(requestBuilder -> requestBuilder.headers(headers -> setHeaders(exchange, optionalSecurityContext.get(), headers)))
                            .build();
                    return chain.filter(mutatedExchange);
                });
    }

    /**
     * This filter runs after {@link HttpRequestSmugglingPreventionFilter} which has {@link Ordered#HIGHEST_PRECEDENCE}.
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 1
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private void setHeaders(ServerWebExchange exchange, SecurityContext securityContext, HttpHeaders headers) {
        if (securityContext.getAuthentication() instanceof BearerTokenAuthentication bearerAuth) {
            Map<String, Object> tokenAttributes = bearerAuth.getTokenAttributes();

            String clientId = getAttribute(tokenAttributes, CLIENT_ID);
            String userId = getAttribute(tokenAttributes, USER_ID);
            String username = getAttribute(tokenAttributes, USER_NAME);

            if (StringUtils.isBlank(clientId) || StringUtils.isBlank(userId) || StringUtils.isBlank(username)) {
                HashMap<String, String> claims = extractJwtClaims(bearerAuth.getToken().getTokenValue());

                if (StringUtils.isBlank(clientId)) {
                    clientId = claims.get(CLIENT_ID);
                }

                userId = getUserId(userId, claims);

                if (StringUtils.isBlank(username)) {
                    username = claims.get(USER_NAME);
                }
            }

            setHeaderAndAttribute(headers, exchange, CLIENT_ID, clientId);
            setHeaderAndAttribute(headers, exchange, USER_ID, userId);
            setHeaderAndAttribute(headers, exchange, USERNAME, username);
        }
    }

    private String getAttribute(Map<String, Object> attributes, String key) {
        return Optional.ofNullable(attributes.get(key))
                .map(Object::toString)
                .orElse(null);
    }

    private HashMap<String, String> extractJwtClaims(String token) {
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String[] chunks = token.split("\\.");
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payload, HashMap.class);
        } catch (Exception e) {
            log.error("Error occurred while extracting JWT claims. Exception: {}", ExceptionUtils.getStackTrace(e));
            return new HashMap<>();
        }
    }

    private String getUserId(String userId, HashMap<String, String> claims) {
        if (StringUtils.isBlank(userId)) {
            Object userIdClaim = claims.get(USER_ID);
            userId = Objects.nonNull(userIdClaim) ? userIdClaim.toString() : userId;
        }
        return userId;
    }

    private void setHeaderAndAttribute(HttpHeaders headers, ServerWebExchange exchange, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            headers.set(key, value);
            exchange.getAttributes().put(key, value);
        }
    }
}
