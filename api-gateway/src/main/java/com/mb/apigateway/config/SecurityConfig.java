package com.mb.apigateway.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Configures WebFlux security for the API Gateway.
 * <p>
 * Disables CSRF, permits a configurable set of public paths (defined in {@code gateway-service.security.permitted-paths}),
 * and enforces opaque token introspection for all other routes. Uses a path-based
 * {@link ReactiveAuthenticationManagerResolver} to delegate token validation to either
 * Keycloak (for most services) or the stock-exchange-service's own introspection endpoint
 * (for stock-exchange and inventory-management routes with their own JWT auth).
 * <p>
 * The Keycloak introspection {@link WebClient} is built with Netty connection pooling,
 * configurable timeouts, and automatic retry on transient
 * {@link OAuth2IntrospectionException}s.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewaySecurityProperties gatewaySecurityProperties;

    @Value("${secure-service.introspection-uri}")
    private String introspectionUri;

    @Value("${gateway-service.client-id}")
    private String clientId;

    @Value("${gateway-service.client-secret}")
    private String clientSecret;

    @Value("${spring.cloud.gateway.server.webflux.httpclient.connect-timeout}")
    private Duration connectTimeout;

    @Value("${spring.cloud.gateway.server.webflux.httpclient.response-timeout}")
    private Duration responseTimeout;

    @Value("${spring.cloud.gateway.server.webflux.httpclient.pool.max-idle-time}")
    private Duration maxIdleTime;

    @Value("${stock-exchange-service.introspection-uri}")
    private String stockExchangeIntrospectionUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(gatewaySecurityProperties.getPermittedPaths().toArray(String[]::new)).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec.authenticationManagerResolver(authenticationManagerResolver()))
                .anonymous(Customizer.withDefaults())
                .build();
    }

    @Bean
    public ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver() {
        ReactiveAuthenticationManager keycloakAuthManager = new OpaqueTokenReactiveAuthenticationManager(opaqueTokenIntrospector());

        ReactiveAuthenticationManager stockExchangeAuthManager = new OpaqueTokenReactiveAuthenticationManager(stockExchangeTokenIntrospector());

        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            if (gatewaySecurityProperties.isStockExchangeAuthPath(path)) {
                return Mono.just(stockExchangeAuthManager);
            }
            return Mono.just(keycloakAuthManager);
        };
    }

    @Bean
    @Primary
    public ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector() {
        ConnectionProvider connectionProviderWithMaxIdleTime = ConnectionProvider.builder("withMaxIdleTime")
                .maxIdleTime(maxIdleTime)
                .build();

        HttpClient httpClient = HttpClient.create(connectionProviderWithMaxIdleTime)
                .responseTimeout(responseTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());

        WebClient webClient = WebClient.builder()
                .defaultHeaders(h -> h.setBasicAuth(clientId, clientSecret))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) ->
                        next
                                .exchange(request)
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)).filter(OAuth2IntrospectionException.class::isInstance))
                )
                .build();

        return new SpringReactiveOpaqueTokenIntrospector(introspectionUri, webClient);
    }

    /**
     * Creates a {@link LoadBalanced} {@link WebClient.Builder} that resolves {@code lb://} URIs
     * via Eureka service discovery. Used by service-specific token introspectors to call
     * downstream introspection endpoints (e.g., {@code lb://stock-exchange-service/...},
     * {@code lb://inventory-management-service/...}) without hardcoding host/port.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ReactiveOpaqueTokenIntrospector stockExchangeTokenIntrospector() {
        return new StockExchangeReactiveTokenIntrospector(loadBalancedWebClientBuilder(), stockExchangeIntrospectionUri);
    }
}
