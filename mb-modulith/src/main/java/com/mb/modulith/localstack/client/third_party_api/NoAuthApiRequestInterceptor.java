package com.mb.modulith.localstack.client.third_party_api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;

import java.time.Instant;

import static org.springframework.security.oauth2.client.registration.ClientRegistration.withRegistrationId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoAuthApiRequestInterceptor {

    public static OAuth2AccessTokenInterceptor noAuthInterceptor(final String registrationId) {
        return new OAuth2AccessTokenInterceptor(
                registrationId,
                _ -> new OAuth2AuthorizedClient(withRegistrationId("default").build(), "anonymous", new OAuth2AccessToken(TokenType.BEARER, "no-auth-token", Instant.now(), Instant.MAX))
        );
    }
}
