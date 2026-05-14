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
 * The {@code stock-exchange-auth-path-prefixes} list defines path prefixes that should use
 * stock-exchange-service token introspection instead of Keycloak.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway-service.security")
public class GatewaySecurityProperties {

    private List<String> permittedPaths = List.of();

    /**
     * Path prefixes routed to stock-exchange-service token introspection.
     * Each entry should include the trailing slash (e.g., "/stock-exchange/", "/inventory/").
     */
    private List<String> stockExchangeAuthPathPrefixes = List.of();

    /**
     * Returns true if the given path should be authenticated via stock-exchange-service.
     */
    public boolean isStockExchangeAuthPath(String path) {
        for (String prefix : stockExchangeAuthPathPrefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
