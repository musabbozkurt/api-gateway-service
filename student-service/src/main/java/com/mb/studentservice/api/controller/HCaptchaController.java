package com.mb.studentservice.api.controller;

import com.mb.studentservice.client.hcaptcha.response.HCaptchaResponse;
import com.mb.studentservice.service.client.hcaptcha.HCaptchaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/validate")
public class HCaptchaController {
    private final HCaptchaService hCaptchaService;

    /**
     * Validate request with HCaptcha client
     *
     * @param captchaResponse gets captcha response.
     */
    @GetMapping
    public HCaptchaResponse validate(@RequestParam(value = "h-captcha-response", defaultValue = "10000000-aaaa-bbbb-cccc-000000000001") String captchaResponse) {
        if (StringUtils.hasText(captchaResponse)) {
            hCaptchaService.validateRequestWithHttpClient(captchaResponse);
            hCaptchaService.validateRequestWithMultiValueMap(captchaResponse);
            hCaptchaService.isHCaptchaResponseValid(captchaResponse);
            return hCaptchaService.validateRequest(captchaResponse);
        }
        return new HCaptchaResponse();
    }
}
