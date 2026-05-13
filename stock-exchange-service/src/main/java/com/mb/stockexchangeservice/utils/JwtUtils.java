package com.mb.stockexchangeservice.utils;

import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtUtils {

    @Value("${testing.app.jwt-secret}")
    private String jwtSecret;

    @Value("${testing.app.jwt-expiration-ms}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = Optional.ofNullable((UserDetails) authentication.getPrincipal()).orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.USER_NOT_FOUND));

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSecretKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            getUserNameFromJwtToken(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.INVALID_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new BaseException(StockExchangeServiceErrorCode.INVALID_JWT_TOKEN);
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
