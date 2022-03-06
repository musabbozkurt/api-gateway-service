package com.mb.studentservice.config.payment;

import com.mb.studentservice.client.payment.PaymentTokenStore;
import com.mb.studentservice.config.ResponseToErrorDecoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class PaymentFeignConfig {

    private final PaymentTokenStore paymentTokenStore;

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        return new PaymentFeignClientRequestInterceptor(paymentTokenStore);
    }

    @Bean
    public ErrorDecoder paymentErrorDecoder() {
        return new ResponseToErrorDecoder();
    }

    @Bean
    public Retryer paymentRetry() {
        return Retryer.NEVER_RETRY;
    }
}
