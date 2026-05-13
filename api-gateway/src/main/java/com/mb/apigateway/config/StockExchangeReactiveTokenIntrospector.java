package com.mb.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * Custom {@link ReactiveOpaqueTokenIntrospector} that validates tokens by calling
 * stock-exchange-service's own introspection endpoint ({@code /api/v1/auth/introspect}).
 *
 * <p>Used as an alternative to Keycloak introspection for services that manage their own JWT auth.</p>
 */
@Slf4j
public class StockExchangeReactiveTokenIntrospector implements ReactiveOpaqueTokenIntrospector {

    private final WebClient webClient;
    private final String introspectionUri;

    public StockExchangeReactiveTokenIntrospector(WebClient.Builder webClientBuilder, String introspectionUri) {
        this.webClient = webClientBuilder.build();
        this.introspectionUri = introspectionUri;
    }

    @Override
    public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
        log.debug("Calling stock-exchange introspection at: {}", introspectionUri);
        return webClient.post()
                .uri(introspectionUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("token=" + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .flatMap(response -> {
                    Boolean active = (Boolean) response.get("active");
                    if (Boolean.TRUE.equals(active)) {
                        String username = (String) response.get("username");
                        log.debug("Stock-exchange token introspection successful for user: {}", username);
                        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(StringUtils.isNotBlank(username) ? username : "unknown", response, Collections.emptyList());
                        return Mono.just(principal);
                    }
                    log.warn("Stock-exchange token introspection returned inactive");
                    return Mono.error(new BadOpaqueTokenException("Token is not active"));
                })
                .onErrorResume(BadOpaqueTokenException.class, Mono::error)
                .onErrorResume(ex -> {
                    log.error("Stock-exchange token introspection failed: {}", ex.getMessage());
                    return Mono.error(new BadOpaqueTokenException("Token introspection failed"));
                });
    }
}
