package com.mb.studentservice.client.hcaptcha.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HCaptchaRequest {

    private String response;

    private String secret;

    @JsonProperty("sitekey")
    private String siteKey;

    @JsonProperty("remoteip")
    private String remoteIp;
}
