package com.mb.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${sso.introspection-endpoint}")
    private String introspectionEndpoint;

    @Value("${sso.client-id}")
    private String clientId;

    @Value("${sso.client-secret}")
    private String clientSecret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .opaqueToken(token -> token
                                .introspectionUri(introspectionEndpoint)
                                .introspectionClientCredentials(clientId, clientSecret)
                        )
                )
                .build();
    }
}
