package com.mb.paymentservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class KeycloakProperties {

    @Value("${KEYCLOAK_BASE_URL}")
    private String authServerUrl;

    @Value("${KEYCLOAK_EXTERNAL_URL}")
    private String externalUrl;

    @Value("${KEYCLOAK_REALM}")
    private String realm;
}
