package com.mb.apigateway.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${sso.introspection-endpoint}")
    private String introspectionEndpoint;

    @Value("${sso.client-id}")
    private String clientId;

    @Value("${sso.client-secret}")
    private String clientSecret;

    @Value("${spring.cloud.gateway.httpclient.connect-timeout}")
    private Duration connectTimeout;

    @Value("${spring.cloud.gateway.httpclient.response-timeout}")
    private Duration responseTimeout;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .opaqueToken(token -> token.introspector(opaqueTokenIntrospector()))
                )
                .build();
    }

    @Bean
    public ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(responseTimeout)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis());

        WebClient webClient = WebClient.builder()
                .defaultHeaders(h -> h.setBasicAuth(clientId, clientSecret))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return new NimbusReactiveOpaqueTokenIntrospector(introspectionEndpoint, webClient);
    }
}
