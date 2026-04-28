package com.mb.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds the {@code gateway-service.security} properties from application configuration.
 * <p>
 * The {@code permitted-paths} list defines URL patterns that are accessible without authentication.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway-service.security")
public class GatewaySecurityProperties {

    private List<String> permittedPaths = List.of();
}
