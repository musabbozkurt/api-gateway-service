package com.mb.studentservice.client.payment;

import com.mb.studentservice.config.payment.PaymentOAuthFeignConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InMemoryPaymentTokenStore implements PaymentTokenStore {

    private String accessToken;
    private final PaymentOAuthFeignConfig paymentOAuthFeignConfig;

    @Override
    public String getPaymentToken() {
        if (StringUtils.isEmpty(accessToken)) {
            String authFeignConfigAccessToken = paymentOAuthFeignConfig.getAccessToken();
            this.accessToken = authFeignConfigAccessToken;
            storePaymentToken(authFeignConfigAccessToken);
            log.info("PaymentToken token is refreshed. token: {}.", this.accessToken);
        }
        return accessToken;
    }

    @Override
    public void storePaymentToken(String token) {
        this.accessToken = token;
    }

    @Override
    public void revokeToken() {
        storePaymentToken(paymentOAuthFeignConfig.getAccessToken());
    }
}
