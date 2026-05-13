package com.mb.stockexchangeservice.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OAuth 2.0 Token Introspection response (RFC 7662 compatible).
 * Used by the API Gateway to validate stock-exchange-service JWT tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenIntrospectionResponse {

    private boolean active;
    private String username;
    private String sub;
    private List<String> roles;
    private Long exp;
    private Long iat;
}
