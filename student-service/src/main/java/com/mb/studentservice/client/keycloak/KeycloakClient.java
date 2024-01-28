package com.mb.studentservice.client.keycloak;

import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.config.keycloak.KeycloakClientConfig;
import com.mb.studentservice.config.keycloak.OAuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "keycloak-client", url = "${GATEWAY_BASE_URL}", configuration = {OAuthFeignConfig.class, KeycloakClientConfig.class})
public interface KeycloakClient {

    @GetMapping(value = "/payments")
    List<PaymentResponse> getPayments();

}
