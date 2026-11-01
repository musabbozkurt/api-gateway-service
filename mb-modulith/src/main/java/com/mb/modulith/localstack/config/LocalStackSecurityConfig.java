package com.mb.modulith.localstack.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security rules for the local stack module routes ({@code /temp-tables/**}).
 * The chains are scoped with a {@code securityMatcher} so that they do not interfere with the other module chains.
 * CSRF is disabled because the chains are stateless and do not rely on session cookies.
 * <p>
 * This class only groups the nested {@link Configuration} classes (picked up by component scanning),
 * so it is not instantiable.
 */
@SuppressWarnings("java:S4502")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalStackSecurityConfig {

    private static final String TEMP_TABLES_PATH = "/temp-tables/**";

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "false")
    static class SecurityConfigurationDisabled {

        @Bean
        @Order(-1)
        public SecurityFilterChain localStackSecurityFilterChain(final HttpSecurity http) {
            return http.securityMatcher(TEMP_TABLES_PATH)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                    .build();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.security.enabled", havingValue = "true", matchIfMissing = true)
    static class SecurityConfigurationEnabled {

        @Bean
        @Order(-1)
        public SecurityFilterChain localStackSecurityFilterChain(final HttpSecurity http) {
            return http.securityMatcher(TEMP_TABLES_PATH)
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(AbstractHttpConfigurer::disable)
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(HttpMethod.GET, TEMP_TABLES_PATH).permitAll()
                            .anyRequest().denyAll())
                    .build();
        }
    }
}
