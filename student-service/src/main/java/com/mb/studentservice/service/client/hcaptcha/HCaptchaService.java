package com.mb.studentservice.service.client.hcaptcha;

import com.mb.studentservice.client.hcaptcha.response.HCaptchaResponse;

public interface HCaptchaService {

    HCaptchaResponse validateRequestWithHttpClient(String captchaResponse);

    HCaptchaResponse validateRequestWithMultiValueMap(String captchaResponse);

    HCaptchaResponse validateRequest(String captchaResponse);

    boolean isHCaptchaResponseValid(String captchaResponse);
}
