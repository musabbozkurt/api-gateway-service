package com.mb.stockexchangeservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stock Exchange Service OpenAPI")
                        .version("0.0")
                        .description("""
                                Stock Exchange Service Swagger
                                
                                **Authentication:**
                                1. Call POST /auth/signin with username and password
                                2. Copy the token from response
                                3. Click 'Authorize' and paste token in bearer-jwt field
                                """)
                        .contact(new Contact()
                                .name("Musab Bozkurt")
                                .url("https://github.com/musabbozkurt")
                                .email("musab.bozkurt@mb.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /auth/signin")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
