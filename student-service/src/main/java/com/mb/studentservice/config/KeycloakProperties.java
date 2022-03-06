package com.mb.studentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("keycloak")
public class KeycloakProperties {

    private String clientId;
    private String clientSecret;
    private String scope;
    private String authorizationGrantType;
    private String authorizationUri;
    private String userInfoUri;
    private String tokenUri;
    private String logoutUri;
    private String jwkSetUri;
    private String certsId;

}
