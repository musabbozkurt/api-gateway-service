package com.mb.apigateway.documentation;

import com.mb.apigateway.config.SwaggerConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.List;
import java.util.stream.Collectors;

@Primary
@Component
@AllArgsConstructor
public class SwaggerDocumentationProvider implements SwaggerResourcesProvider {

    private final SwaggerConfig swaggerConfig;

    @Override
    public List<SwaggerResource> get() {
        return swaggerConfig.getServices()
                .stream()
                .map(swaggerService -> {
                    SwaggerResource swaggerResource = new SwaggerResource();
                    swaggerResource.setName(swaggerService.getName());
                    swaggerResource.setLocation(swaggerService.getUrl());
                    swaggerResource.setSwaggerVersion(swaggerService.getVersion());
                    return swaggerResource;
                })
                .collect(Collectors.toList());
    }

}
