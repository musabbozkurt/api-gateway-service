package com.mb.studentservice.service.client.payment;

import java.util.List;

public interface KeycloakService {

    String getAccessToken(String username, String password);

    String checkAccessTokenValidity(String token);

    void logout(String refreshToken);

    List<String> getRoles(String token);

    String getUserInfo(String token);
}
