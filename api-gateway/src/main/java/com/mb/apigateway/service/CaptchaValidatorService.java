package com.mb.apigateway.service;

public interface CaptchaValidatorService {

    boolean validateCaptcha(String captchaResponse);

}
