package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.api.response.TokenIntrospectionResponse;
import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.service.AuthService;
import com.mb.stockexchangeservice.service.TokenStore;
import com.mb.stockexchangeservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public JwtResponse getJwtResponse(ApiUserAuthRequest apiUserAuthRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(apiUserAuthRequest.getUsername(), apiUserAuthRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenStore.getAccessToken(authentication);

        UserDetails userDetails = Optional.ofNullable((UserDetails) authentication.getPrincipal()).orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.BAD_CREDENTIALS));

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt, userDetails.getUsername(), roles);
    }

    @Override
    public TokenIntrospectionResponse introspectToken(String token) {
        try {
            if (StringUtils.isBlank(token) || !jwtUtils.validateJwtToken(token)) {
                return TokenIntrospectionResponse.builder().active(false).build();
            }

            String username = jwtUtils.getUserNameFromJwtToken(token);
            List<String> roles = userDetailsService.loadUserByUsername(username)
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return TokenIntrospectionResponse.builder()
                    .active(true)
                    .username(username)
                    .sub(username)
                    .roles(roles)
                    .build();
        } catch (Exception e) {
            log.warn("Token introspection failed. Exception: {}", ExceptionUtils.getStackTrace(e));
            return TokenIntrospectionResponse.builder().active(false).build();
        }
    }
}
