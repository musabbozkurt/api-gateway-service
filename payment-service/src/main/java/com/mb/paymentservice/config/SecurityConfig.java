package com.mb.paymentservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Value(value = "${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${spring.security.oauth2.resource-server.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resource-server.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers(
                                "/",
                                apiDocsPath,
                                "%s/**".formatted(apiDocsPath),
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/actuator/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    /**
     * Custom JwtDecoder that fetches signing keys from the internal Docker-reachable {@code jwk-set-uri}
     * while validating the JWT {@code iss} claim against the external browser-facing {@code issuer-uri}.
     * This avoids the default behavior of fetching the OIDC discovery document from the issuer URL,
     * which is unreachable from inside Docker (e.g., {@code http://localhost:9090}).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return jwtDecoder;
    }
}
