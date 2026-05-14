package com.mb.brokerageprovider.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Brokerage Provider OpenAPI")
                        .version("0.0")
                        .description("Brokerage Provider Swagger")
                        .contact(new Contact()
                                .name("Musab Bozkurt")
                                .url("https://github.com/musabbozkurt")
                                .email("musab.bozkurt@mb.com")))
                .tags(List.of(
                                new Tag().name("User").description("Endpoints for CRUD operations on users"),
                                new Tag().name("Order").description("Endpoints for CRUD operations on orders"),
                                new Tag().name("Stock").description("Endpoints for CRUD operations on stocks")
                        )
                );
    }
}
