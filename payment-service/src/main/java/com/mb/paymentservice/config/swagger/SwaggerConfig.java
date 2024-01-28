package com.mb.paymentservice.config.swagger;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public Info info() {
        return new Info().title("Payment Service API")
                .description("Payment Service API")
                .version("1.0")
                .license(getLicense());
    }

    private License getLicense() {
        return new License()
                .name("Unlicensed")
                .url("https://unlicense.org/");
    }
}
