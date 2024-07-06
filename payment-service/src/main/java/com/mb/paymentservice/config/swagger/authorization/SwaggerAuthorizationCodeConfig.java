package com.mb.paymentservice.config.swagger.authorization;

import com.mb.paymentservice.config.KeycloakProperties;
import com.mb.paymentservice.config.swagger.SwaggerConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
@ConditionalOnProperty(name = "springdoc.security.config.auth-code-flow", havingValue = "true")
public class SwaggerAuthorizationCodeConfig {

    private static final String OAUTH_SCHEME_NAME = "OAuth";
    private static final String OPENID_CONNECT_URL_FORMAT = "%s/realms/%s/protocol/openid-connect";

    @Bean
    OpenAPI customOpenApi(KeycloakProperties keycloakProperties, SwaggerConfig swaggerConfig) {
        return new OpenAPI()
                .info(swaggerConfig.info())
                .components(new Components()
                        .addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme(keycloakProperties)))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME));
    }

    private SecurityScheme createOAuthScheme(KeycloakProperties properties) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(createOAuthFlows(properties));
    }

    private OAuthFlows createOAuthFlows(KeycloakProperties properties) {
        return new OAuthFlows().authorizationCode(createAuthorizationCodeFlow(properties));
    }

    private OAuthFlow createAuthorizationCodeFlow(KeycloakProperties properties) {
        var protocolUrl = String.format(OPENID_CONNECT_URL_FORMAT, properties.getAuthServerUrl(), properties.getRealm());

        return new OAuthFlow()
                .authorizationUrl(protocolUrl)
                .tokenUrl(protocolUrl + "/token")
                .scopes(new Scopes());
    }
}
