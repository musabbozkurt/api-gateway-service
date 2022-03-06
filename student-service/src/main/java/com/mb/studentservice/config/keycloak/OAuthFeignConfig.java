package com.mb.studentservice.config.keycloak;

import com.mb.studentservice.config.OAuthProperties;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@Configuration
@RequiredArgsConstructor
public class OAuthFeignConfig {

    private final OAuthProperties properties;

    @Bean
    public RequestInterceptor requestInterceptor() {
        ClientRegistration clientRegistration = clientRegistrationRepository().findByRegistrationId(properties.getClientRegistrationId());
        OAuthClientCredentialsFeignManager clientCredentialsFeignManager =
                new OAuthClientCredentialsFeignManager(authorizedClientManager(), clientRegistration);
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + clientCredentialsFeignManager.getAccessToken());
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager() {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository(), oAuth2AuthorizedClientService());
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(properties.getClientId());

        ClientRegistration registration = builder.clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .tokenUri(properties.getTokenUri())
                .authorizationGrantType(CLIENT_CREDENTIALS)
                .build();

        return new InMemoryClientRegistrationRepository(List.of(registration));
    }
}