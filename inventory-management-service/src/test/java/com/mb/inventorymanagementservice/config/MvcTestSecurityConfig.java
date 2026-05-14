package com.mb.inventorymanagementservice.config;

import com.mb.inventorymanagementservice.config.security.AuthTokenFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Lightweight security configuration for {@code @WebMvcTest} slices.
 * <p>
 * Enables method-level security ({@code @PreAuthorize}) and removes the
 * {@code ROLE_} prefix so that {@code hasRole('GET_PRODUCT')} matches
 * an authority named {@code GET_PRODUCT}. Does NOT include the production
 * {@link AuthTokenFilter} in the security filter chain because
 * {@code @WithMockUser} sets the SecurityContext directly.
 * <p>
 * The {@link #disableAuthTokenFilterAutoRegistration} bean prevents Spring Boot
 * from auto-registering the {@link AuthTokenFilter} as a servlet filter, which
 * would otherwise interfere with {@code @WithMockUser} context propagation.
 * <p>
 * The {@link #securityMockMvcCustomizer()} bean ensures Spring Security's test
 * support (e.g. {@code @WithMockUser}) is integrated with MockMvc via
 * {@code SecurityMockMvcConfigurers.springSecurity()}.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class MvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl()))
                .build();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    /**
     * Applies Spring Security's test support to MockMvc so that {@code @WithMockUser}
     * and {@code SecurityMockMvcRequestPostProcessors} properly propagate the
     * SecurityContext through the filter chain.
     */
    @Bean
    public MockMvcBuilderCustomizer securityMockMvcCustomizer() {
        return builder -> builder.apply(springSecurity());
    }

    /**
     * Prevents Spring Boot from auto-registering {@link AuthTokenFilter} as a servlet filter.
     * The filter is still created as a bean (because it's a {@code @Service} picked up by
     * {@code @WebMvcTest}), but it won't run on requests. Authentication in tests is handled
     * by {@code @WithMockUser} instead.
     */
    @Bean
    public FilterRegistrationBean<AuthTokenFilter> disableAuthTokenFilterAutoRegistration(AuthTokenFilter authTokenFilter) {
        FilterRegistrationBean<AuthTokenFilter> registration = new FilterRegistrationBean<>(authTokenFilter);
        registration.setEnabled(false);
        return registration;
    }
}
