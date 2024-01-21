package com.mb.studentservice.client.hcaptcha;

import com.mb.studentservice.client.hcaptcha.request.HCaptchaRequest;
import com.mb.studentservice.client.hcaptcha.response.HCaptchaResponse;
import com.mb.studentservice.config.hcaptcha.HCaptchaCoreFeignConfiguration;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@FeignClient(name = "hcaptcha", url = "${feign.services.hcaptcha.url}", configuration = HCaptchaCoreFeignConfiguration.class)
public interface HCaptchaClient {

    @PostMapping(value = "/siteverify", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    HCaptchaResponse validateRequest(@RequestBody MultiValueMap<String, ?> hCaptchaRequest);

    @PostMapping(value = "/siteverify", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    HCaptchaResponse validateRequest(HCaptchaRequest hCaptchaRequest);

}
