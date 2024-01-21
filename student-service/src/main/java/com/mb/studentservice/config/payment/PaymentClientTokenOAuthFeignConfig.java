package com.mb.studentservice.config.payment;

import com.mb.studentservice.config.OAuthProperties;
import com.mb.studentservice.config.keycloak.OAuthClientCredentialsFeignManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@Component
@RequiredArgsConstructor
public class PaymentClientTokenOAuthFeignConfig {

    private final OAuthProperties properties;

    public String getAccessToken() {
        ClientRegistration clientRegistration = clientRegistrationRepository().findByRegistrationId(properties.getClientRegistrationId());
        OAuthClientCredentialsFeignManager clientCredentialsFeignManager =
                new OAuthClientCredentialsFeignManager(authorizedClientManager(), clientRegistration);
        return clientCredentialsFeignManager.getAccessToken();
    }

    private OAuth2AuthorizedClientManager authorizedClientManager() {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository(), oAuth2AuthorizedClientService());
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    private ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(properties.getClientId());

        ClientRegistration registration = builder.clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .tokenUri(properties.getTokenUri())
                .authorizationGrantType(CLIENT_CREDENTIALS)
                .build();

        return new InMemoryClientRegistrationRepository(List.of(registration));
    }
}