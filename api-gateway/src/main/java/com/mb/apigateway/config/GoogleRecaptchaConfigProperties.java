package com.mb.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "google.recaptcha")
public class GoogleRecaptchaConfigProperties {

    private String url;
    private KeyInfo key = new KeyInfo();

    @Data
    public static class KeyInfo {
        private String site;
        private String secret;
        private float threshold;
    }
}
