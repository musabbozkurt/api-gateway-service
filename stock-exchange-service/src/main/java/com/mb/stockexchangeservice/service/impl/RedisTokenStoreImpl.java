package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.service.TokenStore;
import com.mb.stockexchangeservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "token.store", havingValue = "redis")
public class RedisTokenStoreImpl implements TokenStore {

    private static final String ACCESS_TOKEN = "jwt:access_token";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    @Override
    public String getAccessToken(Authentication authentication) {
        String accessToken = redisTemplate.opsForValue().get(ACCESS_TOKEN);
        if (StringUtils.isEmpty(accessToken)) {
            accessToken = jwtUtils.generateJwtToken(authentication);
            storeAccessToken(accessToken);
        }
        return accessToken;
    }

    @Override
    public void storeAccessToken(String token) {
        redisTemplate.opsForValue().set(ACCESS_TOKEN, token, Duration.ofMinutes(10));
    }
}
