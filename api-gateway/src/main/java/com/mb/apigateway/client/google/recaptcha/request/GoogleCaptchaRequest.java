package com.mb.apigateway.client.google.recaptcha.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GoogleCaptchaRequest {

    private String secret;
    private String response;

}
