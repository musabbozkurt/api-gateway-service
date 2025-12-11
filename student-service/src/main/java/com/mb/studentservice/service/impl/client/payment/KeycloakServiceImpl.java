package com.mb.studentservice.service.impl.client.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.studentservice.client.payment.response.KeycloakAccessTokenResponse;
import com.mb.studentservice.config.KeycloakProperties;
import com.mb.studentservice.service.client.payment.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final RestTemplate restTemplate;
    private final KeycloakProperties properties;

    public String getAccessToken(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        map.add("client_id", properties.getClientId());
        map.add("grant_type", properties.getAuthorizationGrantType());
        map.add("client_secret", properties.getClientSecret());
        map.add("scope", properties.getScope());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        KeycloakAccessTokenResponse keycloakAccessTokenResponse = restTemplate.postForObject(properties.getTokenUri(), request, KeycloakAccessTokenResponse.class);
        if (keycloakAccessTokenResponse != null) {
            return keycloakAccessTokenResponse.getAccessToken();
        }
        return "";
    }

    public String checkAccessTokenValidity(String token) {
        return getUserInfo(token);
    }

    public void logout(String refreshToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("client_id", properties.getClientId());
        httpHeaders.add("client_secret", properties.getClientSecret());
        httpHeaders.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, httpHeaders);
        restTemplate.postForObject(properties.getLogoutUri(), request, String.class);
    }

    public List<String> getRoles(String token) {
        String response = getUserInfo(token);
        HashMap<String, List<String>> map = new HashMap<>();
        try {
            map = new ObjectMapper().readValue(response, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return map.get("roles");
    }

    public String getUserInfo(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, httpHeaders);
        return restTemplate.postForObject(properties.getUserInfoUri(), request, String.class);
    }
}
