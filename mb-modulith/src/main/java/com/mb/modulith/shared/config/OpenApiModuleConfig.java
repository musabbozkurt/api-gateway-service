package com.mb.modulith.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(OpenApiModuleConfig.SwaggerServices.class)
public class OpenApiModuleConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI mbModulithOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MB Modulith API")
                        .description("""
                                Unified API for school, clinic, player market, email, warehouse, commerce, and delivery modules.
                                Click Authorize and enter a registered username/password (e.g. the seeded demo account
                                username=MBMUSEMENT) to call secured endpoints directly from Swagger UI.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("MB Platform")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME,
                        new SecurityScheme()
                                .name(BASIC_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic authentication using your username and password.")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }

    @Bean
    GroupedOpenApi schoolApi() {
        return GroupedOpenApi.builder()
                .group("school-api")
                .displayName("School Registration")
                .pathsToMatch("/students/**", "/courses/**", "/students-by-course-name/**")
                .build();
    }

    @Bean
    GroupedOpenApi clinicApi() {
        return GroupedOpenApi.builder()
                .group("clinic-api")
                .displayName("Clinic")
                .pathsToMatch("/patient/**", "/doctor/**", "/appointments/**", "/all")
                .build();
    }

    @Bean
    GroupedOpenApi playermarketApi() {
        return GroupedOpenApi.builder()
                .group("playermarket-api")
                .displayName("Player Market")
                .pathsToMatch("/teams/**", "/players/**", "/fee/**", "/soccer/**")
                .build();
    }

    @Bean
    GroupedOpenApi emailApi() {
        return GroupedOpenApi.builder()
                .group("email-api")
                .displayName("Email")
                .pathsToMatch("/client/**", "/emails/**")
                .build();
    }

    @Bean
    GroupedOpenApi warehouseApi() {
        return GroupedOpenApi.builder()
                .group("warehouse-api")
                .displayName("Food Warehouse")
                .pathsToMatch("/api/foods/**")
                .build();
    }

    @Bean
    GroupedOpenApi commerceApi() {
        return GroupedOpenApi.builder()
                .group("commerce-api")
                .displayName("Commerce")
                .pathsToMatch("/api/auth/**", "/api/client/**", "/api/orders/**", "/api/client/address/**")
                .build();
    }

    @Bean
    GroupedOpenApi deliveryApi() {
        return GroupedOpenApi.builder()
                .group("delivery-api")
                .displayName("Delivery logistics")
                .pathsToMatch("/api/person/**", "/api/delivery/**")
                .build();
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "swagger.documentation")
    public static class SwaggerServices {
        private List<ServiceEntry> services;

        @Getter
        @Setter
        public static class ServiceEntry {
            private String name;
            private String displayName;
        }
    }
}
