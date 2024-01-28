package com.mb.swaggerapplication.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Swagger Application API")
                        .description("Swagger Application API")
                        .version("1.0")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }

}