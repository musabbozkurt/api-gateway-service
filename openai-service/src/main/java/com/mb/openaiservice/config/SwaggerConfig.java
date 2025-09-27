package com.mb.openaiservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                description = "OpenAPI documentation for ${spring.application.name}"
        ),
        servers = {
                @Server(url = "${swagger.server.url:}")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Parameter usernameParam = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("username")
                    .required(false);
            operation.addParametersItem(usernameParam);

            Parameter userIdParam = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("userId")
                    .required(false);
            operation.addParametersItem(userIdParam);

            return operation;
        };
    }
}
