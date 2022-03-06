package com.mb.studentservice.service;

public interface TokenStoreService {

    String getPaymentToken();

    void storePaymentToken(String token);

    void revokeToken();

}
