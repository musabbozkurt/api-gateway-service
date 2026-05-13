package com.mb.stockexchangeservice.service;

import org.springframework.security.core.Authentication;

public interface TokenStore {

    String getAccessToken(Authentication authentication);

    void storeAccessToken(String token);
}
