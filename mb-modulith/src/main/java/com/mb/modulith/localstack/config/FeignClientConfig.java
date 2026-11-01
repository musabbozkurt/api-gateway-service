package com.mb.modulith.localstack.config;

import com.mb.modulith.localstack.client.third_party_api.NoAuthApiRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RemoveAuthorizedClientOAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

import java.util.Set;

@Slf4j
@Configuration
@Import(FeignClientsConfiguration.class)
public class FeignClientConfig {

    /**
     * indicates the name of the service that holds the oauth2 config, must be equal to:
     * spring.security.oauth2.client.registration.<SERVICE_NAME>
     */
    private static final String OAUTH_REG_ID = "m2m-internal";

    // for more details see:
    // https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#oauth2-support
    @Bean("integrationOAuth2AuthorizedClientManager")
    public OAuth2AuthorizedClientManager integrationOAuth2AuthorizedClientManager(final OAuth2ClientProperties properties) {
        final OAuth2ClientPropertiesMapper propertiesMapper = new OAuth2ClientPropertiesMapper(properties);
        final ClientRegistrationRepository clientRegistrationRepository = new InMemoryClientRegistrationRepository(propertiesMapper.asClientRegistrations());
        final OAuth2AuthorizedClientService authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        final AuthorizedClientServiceOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        // providers order is important - refresh token must be first
        oAuth2AuthorizedClientManager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().clientCredentials().build());
        oAuth2AuthorizedClientManager.setAuthorizationFailureHandler(getAuthorizationFailureHandler(authorizedClientService));
        return oAuth2AuthorizedClientManager;
    }

    private RemoveAuthorizedClientOAuth2AuthorizationFailureHandler getAuthorizationFailureHandler(final OAuth2AuthorizedClientService authorizedClientService) {
        return new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, _) -> {
                    log.error("Removing authorized client for clientRegistrationId: {}", clientRegistrationId);
                    authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName());
                },
                Set.of(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        OAuth2ErrorCodes.INVALID_GRANT,
                        OAuth2ErrorCodes.SERVER_ERROR,
                        OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE,
                        OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
    }

    @Bean("m2mInternalApiRequestInterceptor")
    @ConditionalOnProperty(value = "spring.security.oauth2.enabled", matchIfMissing = true)
    public OAuth2AccessTokenInterceptor m2mInternalApiRequestInterceptor(@Qualifier("integrationOAuth2AuthorizedClientManager") final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        return new OAuth2AccessTokenInterceptor(OAUTH_REG_ID, oAuth2AuthorizedClientManager);
    }

    @Bean("m2mInternalApiRequestInterceptor")
    @ConditionalOnProperty(value = "spring.security.oauth2.enabled", havingValue = "false")
    public OAuth2AccessTokenInterceptor m2mInternalNoAuthApiRequestInterceptor() {
        return NoAuthApiRequestInterceptor.noAuthInterceptor(OAUTH_REG_ID);
    }
}
