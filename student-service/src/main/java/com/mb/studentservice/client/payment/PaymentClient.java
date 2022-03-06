package com.mb.studentservice.client.payment;

import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.config.payment.PaymentFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "payment-client", url = "${feign.services.payment-client.url}", configuration = PaymentFeignConfig.class)
public interface PaymentClient {

    @GetMapping(value = "/payments")
    List<PaymentResponse> getPayments();

}
