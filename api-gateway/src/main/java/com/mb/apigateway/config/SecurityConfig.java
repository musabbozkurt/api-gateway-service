package com.mb.apigateway.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Configures WebFlux security for the API Gateway.
 * <p>
 * Disables CSRF, permits a configurable set of public paths (defined in {@code gateway-service.security.permitted-paths}),
 * and enforces opaque token introspection for all other routes. The introspection {@link org.springframework.web.reactive.function.client.WebClient}
 * is built with Netty connection pooling, configurable timeouts, and automatic retry on transient
 * {@link org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException}s.
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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(gatewaySecurityProperties.getPermittedPaths().toArray(String[]::new)).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .opaqueToken(token -> token.introspector(opaqueTokenIntrospector()))
                )
                .anonymous(Customizer.withDefaults())
                .build();
    }

    @Bean
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
}
