package com.mb.studentservice.client.payment;

public interface PaymentTokenStore {

    String getPaymentToken();

    void storePaymentToken(String token);

    void revokeToken();

}
