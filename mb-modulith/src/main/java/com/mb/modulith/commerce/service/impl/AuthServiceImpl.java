package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.api.model.request.ApiClientAuthRequest;
import com.mb.modulith.commerce.api.model.response.JwtResponse;
import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.security.services.UserDetailsImpl;
import com.mb.modulith.commerce.service.AuthService;
import com.mb.modulith.commerce.service.TokenStore;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;

    @Override
    public JwtResponse getJwtResponseResponseEntity(ApiClientAuthRequest apiClientAuthRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(apiClientAuthRequest.getUsername(), apiClientAuthRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenStore.getAccessToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (Objects.isNull(userDetails)) {
            throw new BaseException(SharedErrorCode.NOT_FOUND);
        }
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }
}
