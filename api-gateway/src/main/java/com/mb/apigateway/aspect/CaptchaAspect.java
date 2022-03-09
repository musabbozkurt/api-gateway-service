package com.mb.apigateway.aspect;

import com.mb.apigateway.service.CaptchaValidatorService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class CaptchaAspect {

    private static final String CAPTCHA_HEADER_NAME = "captcha-response";

    private final CaptchaValidatorService captchaValidatorService;

    @Around("@annotation(com.mb.apigateway.aspect.annotation.RequiresCaptcha)")
    public Object validateCaptcha(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String captchaResponse = request.getHeader(CAPTCHA_HEADER_NAME);
        boolean isValidCaptcha = captchaValidatorService.validateCaptcha(captchaResponse);
        if (!isValidCaptcha) {
            throw new RuntimeException("Invalid captcha");
        }
        return joinPoint.proceed();
    }

}
