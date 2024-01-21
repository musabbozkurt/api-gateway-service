package com.mb.studentservice.config.hcaptcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feign.services.hcaptcha")
public class HCaptchaProperties {

    private String url;
    private String secret;
    private String response;
    private String siteKey;
    private String remoteIp;
    private float riskScoreThreshold;

}