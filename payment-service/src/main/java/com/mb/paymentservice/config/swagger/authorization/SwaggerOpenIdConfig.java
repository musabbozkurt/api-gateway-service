package com.mb.paymentservice.config.swagger.authorization;

import com.mb.paymentservice.config.KeycloakProperties;
import com.mb.paymentservice.config.swagger.SwaggerConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
@ConditionalOnProperty(name = "springdoc.security.config.openid-discovery", havingValue = "true")
public class SwaggerOpenIdConfig {

    private static final String OPEN_ID_SCHEME_NAME = "OpenId";
    private static final String OPENID_CONFIGURATION_URL_FORMAT = "%s/realms/%s/.well-known/openid-configuration";

    @Bean
    OpenAPI customOpenApi(KeycloakProperties keycloakProperties, SwaggerConfig swaggerConfig) {
        return new OpenAPI()
                .info(swaggerConfig.info())
                .components(new Components()
                        .addSecuritySchemes(OPEN_ID_SCHEME_NAME, createOpenIdScheme(keycloakProperties)))
                .addSecurityItem(new SecurityRequirement().addList(OPEN_ID_SCHEME_NAME));
    }

    private SecurityScheme createOpenIdScheme(KeycloakProperties properties) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OPENIDCONNECT)
                .openIdConnectUrl(String.format(OPENID_CONFIGURATION_URL_FORMAT, properties.getAuthServerUrl(), properties.getRealm()));
    }
}
