package com.mb.paymentservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class KeycloakProperties {

    @Value("${KEYCLOAK_BASE_URL}")
    private String authServerUrl;

    @Value("${PAYMENT_SERVICE_REALM}")
    private String realm;
}
