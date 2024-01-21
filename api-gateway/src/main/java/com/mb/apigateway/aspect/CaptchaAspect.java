package com.mb.apigateway.aspect;

import com.mb.apigateway.service.CaptchaValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CaptchaAspect {

    private static final String CAPTCHA_HEADER_NAME = "captcha-response";
    private static final List<String> EXCLUDED_URIS = List.of("/api-docs");

    private final CaptchaValidatorService captchaValidatorService;

    @Around("@annotation(com.mb.apigateway.aspect.annotation.RequiresCaptcha)")
    public Object validateCaptcha(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();

        if (EXCLUDED_URIS.stream().anyMatch(requestURI::endsWith)) {
            log.info("Captcha validation is skipped for excluded URI: {}", requestURI);
            return joinPoint.proceed();
        }

        boolean isValidCaptcha = captchaValidatorService.validateCaptcha(request.getHeader(CAPTCHA_HEADER_NAME));
        if (!isValidCaptcha) {
            throw new RuntimeException("Invalid captcha");
        }
        return joinPoint.proceed();
    }

}
