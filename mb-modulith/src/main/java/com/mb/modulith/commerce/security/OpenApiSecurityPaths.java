package com.mb.modulith.commerce.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Paths served by SpringDoc OpenAPI / Swagger UI (including WebJars static assets).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenApiSecurityPaths {

    public static boolean matches(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return path.equals("/")
                || path.equals("/swagger-ui.html")
                || path.equals("/error")
                || path.equals("/favicon.ico")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/webjars/");
    }
}
