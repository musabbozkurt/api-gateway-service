/**
 * Security configuration for the API Gateway using Spring Security and OAuth2.
 * This configuration sets up route-based access control, disables CSRF protection,
 * and configures an opaque token introspector with custom WebClient settings.
 * It allows unauthenticated access to specific endpoints while securing all other routes.

package com.mb.apigateway.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] PERMITTED_PATHS = {
            "/actuator/health",
            "/rbac-service/api/v1/forgot-password/generate-code",
            "/rbac-service/api/v1/forgot-password/validate-code",
            "/rbac-service/api/v1/forgot-password/change"
    };

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
                        .pathMatchers(PERMITTED_PATHS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .opaqueToken(token -> token.introspector(opaqueTokenIntrospector()))
                )
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
*/
