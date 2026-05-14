package com.mb.stockexchangeservice.api.controller;

import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.api.response.TokenIntrospectionResponse;
import com.mb.stockexchangeservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody ApiUserAuthRequest apiUserAuthRequest) {
        log.info("Received a request to authenticate user. authenticateUser - ApiUserAuthRequest: {}", apiUserAuthRequest);
        return ResponseEntity.ok(authService.getJwtResponse(apiUserAuthRequest));
    }

    /**
     * Token introspection endpoint (RFC 7662 compatible).
     * Called by the API Gateway to validate stock-exchange-service JWT tokens.
     * Accepts the token as a form/query parameter per RFC 7662.
     */
    @PostMapping("/introspect")
    public ResponseEntity<TokenIntrospectionResponse> introspectToken(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.introspectToken(token));
    }
}
