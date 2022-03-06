package com.mb.studentservice.service.impl.client.payment;

import com.mb.studentservice.service.TokenStoreService;
import com.mb.studentservice.config.payment.PaymentClientTokenOAuthFeignConfig;
import com.mb.studentservice.service.client.payment.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InMemoryPaymentTokenStoreService implements TokenStoreService {

    private String accessToken;
    private final PaymentClientTokenOAuthFeignConfig paymentClientTokenOAuthFeignConfig;
    private final KeycloakService keycloakService;

    @Override
    public String getPaymentToken() {
        String keycloakAccessToken = keycloakService.getAccessToken("payment-service-user", "test");
        if (StringUtils.isNotEmpty(keycloakAccessToken)) {
            return keycloakAccessToken;
        }

        if (StringUtils.isEmpty(accessToken)) {
            String authFeignConfigAccessToken = paymentClientTokenOAuthFeignConfig.getAccessToken();
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
        storePaymentToken(paymentClientTokenOAuthFeignConfig.getAccessToken());
    }
}
