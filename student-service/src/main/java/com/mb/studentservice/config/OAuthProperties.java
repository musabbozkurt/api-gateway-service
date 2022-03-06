package com.mb.studentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("spring.security.oauth2.client.provider.keycloak")
public class OAuthProperties {

    private String tokenUri;
    private String clientId;
    private String clientSecret;
    private String authorizationGrantType;
    private String clientRegistrationId;

}
