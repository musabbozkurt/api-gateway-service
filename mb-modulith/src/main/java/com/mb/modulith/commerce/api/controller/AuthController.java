package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiClientAuthRequest;
import com.mb.modulith.commerce.api.model.response.JwtResponse;
import com.mb.modulith.commerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    @SecurityRequirements
    @Operation(
            summary = "Sign in with username/password.",
            description = """
                    Public endpoint. Also used by Swagger UI's Basic Auth prompt:
                    your username/password are sent with every request and validated against this same login path.
                    """
    )
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody ApiClientAuthRequest apiClientAuthRequest) {
        return ResponseEntity.ok(authService.getJwtResponseResponseEntity(apiClientAuthRequest));
    }
}
