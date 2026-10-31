package com.mb.modulith.commerce.security;

import com.mb.modulith.commerce.security.jwt.AuthEntryPointJwt;
import com.mb.modulith.commerce.security.jwt.AuthTokenFilter;
import com.mb.modulith.commerce.security.jwt.JwtUtils;
import com.mb.modulith.commerce.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

/**
 * Multi-chain Spring Security setup for commerce API, OpenAPI UI, and other module routes.
 * CSRF is intentionally disabled on each chain: every chain uses {@link SessionCreationPolicy#STATELESS}
 * and authenticates via {@code Authorization} (Bearer JWT or HTTP Basic), not session cookies—so CSRF does not apply.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@SuppressWarnings("java:S4502")
public class WebSecurityConfig {

    private static final RequestMatcher DOCUMENTATION_REQUEST_MATCHER = request -> OpenApiSecurityPaths.matches(request.getRequestURI());

    private static final RequestMatcher NON_API_APPLICATION_MATCHER = request -> {
        String path = request.getRequestURI();
        return !OpenApiSecurityPaths.matches(path) && !path.startsWith("/api/");
    };

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(JwtUtils jwtUtils,
                                                        UserDetailsServiceImpl userDetailsService,
                                                        HandlerExceptionResolver handlerExceptionResolver) {
        return new AuthTokenFilter(jwtUtils, userDetailsService, handlerExceptionResolver);
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain documentationSecurityFilterChain(HttpSecurity http) {
        // OpenAPI/Swagger UI: stateless, no cookie-based auth.
        http.securityMatcher(DOCUMENTATION_REQUEST_MATCHER)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Commerce REST API under {@code /api/**}.
     * This chain is strictly stateless and relies solely on JWTs transmitted via the {@code Authorization: Bearer} header.
     * Since no session cookies or browser-managed authentication states are used for these endpoints,
     * CSRF protection is disabled for this specific filter chain.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain commerceApiSecurityFilterChain(HttpSecurity http,
                                                              AuthTokenFilter authenticationJwtTokenFilter,
                                                              CorsConfigurationSource corsConfigurationSource) {
        http.securityMatcher(request -> request.getRequestURI().startsWith("/api/"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated());

        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Other module routes (clinic, school, playermarket, email client, etc.): stateless, JWT/Basic auth only.
     * CSRF protection is disabled because these endpoints never rely on session cookies - every request is
     * authenticated via the {@code Authorization} header (Bearer JWT or Basic), so there is no session to forge.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http, AuthTokenFilter authenticationJwtTokenFilter) {
        http.securityMatcher(NON_API_APPLICATION_MATCHER)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
