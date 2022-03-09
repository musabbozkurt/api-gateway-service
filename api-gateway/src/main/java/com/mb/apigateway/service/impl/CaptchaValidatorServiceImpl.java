package com.mb.apigateway.service.impl;

import com.mb.apigateway.client.google.recaptcha.response.GoogleCaptchaResponse;
import com.mb.apigateway.config.GoogleRecaptchaConfigProperties;
import com.mb.apigateway.service.CaptchaValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaValidatorServiceImpl implements CaptchaValidatorService {

    private final GoogleRecaptchaConfigProperties googleRecaptchaConfigProperties;
    private final RestTemplate restTemplate;

    public boolean validateCaptcha(String captchaResponse) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", googleRecaptchaConfigProperties.getKey().getSecret());
        requestMap.add("response", captchaResponse);

        GoogleCaptchaResponse apiResponse = restTemplate.postForObject(googleRecaptchaConfigProperties.getUrl(), requestMap, GoogleCaptchaResponse.class);
        log.info("Captcha api response : {}", apiResponse);
        if (apiResponse == null) {
            return false;
        }

        return apiResponse.getSuccess();
    }

}
