package com.mb.paymentservice.config.swagger.authorization;

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
@ConditionalOnProperty(name = "springdoc.security.config.bearer", havingValue = "true")
public class SwaggerBearerConfig {

    private static final String BEARER_AUTH_SCHEME_NAME = "BearerAuth";
    private static final String SCHEME = "bearer";

    @Bean
    OpenAPI customOpenApi(SwaggerConfig swaggerConfig) {
        return new OpenAPI()
                .info(swaggerConfig.info())
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME_NAME, createBearerScheme()))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_NAME));
    }

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(SCHEME);
    }
}
