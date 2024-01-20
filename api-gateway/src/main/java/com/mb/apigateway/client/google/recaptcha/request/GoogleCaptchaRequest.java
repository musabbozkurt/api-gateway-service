package com.mb.apigateway.client.google.recaptcha.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCaptchaRequest {

    private String secret;
    private String response;

}
