package com.mb.stockexchangeservice.service;

import com.mb.stockexchangeservice.api.request.ApiUserAuthRequest;
import com.mb.stockexchangeservice.api.response.JwtResponse;
import com.mb.stockexchangeservice.api.response.TokenIntrospectionResponse;

public interface AuthService {

    JwtResponse getJwtResponse(ApiUserAuthRequest apiUserAuthRequest);

    TokenIntrospectionResponse introspectToken(String token);
}
