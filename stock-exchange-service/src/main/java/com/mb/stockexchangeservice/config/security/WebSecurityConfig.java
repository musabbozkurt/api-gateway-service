package com.mb.stockexchangeservice.config.security;

import com.mb.stockexchangeservice.service.impl.UserDetailsServiceImpl;
import com.mb.stockexchangeservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final String[] ALLOWED_ENDPOINT_PATTERNS = {
            "/h2-console/**", "/swagger-resources", "/swagger-ui.html", "/swagger-ui/index.html", "/swagger-ui/**",
            "/v3/api-docs/**", "/api-docs/**", "/stock-exchange/api-docs/**", "/actuator/**",
            "/api/v1/auth/signin", "/api/v1/auth/signup", "/api/v1/auth/introspect", "/"
    };

    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        return new AuthTokenFilter(jwtUtils, userDetailsService, handlerExceptionResolver);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(unauthorizedHandler))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(ALLOWED_ENDPOINT_PATTERNS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(authenticationJwtTokenFilter(jwtUtils, userDetailsService, handlerExceptionResolver), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        log.info("remove prefix 'ROLE_' from grantedAuthorityDefaults");
        return new GrantedAuthorityDefaults("");
    }
}
