package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.api.model.request.ApiClientAuthRequest;
import com.mb.modulith.commerce.api.model.response.JwtResponse;

public interface AuthService {

    JwtResponse getJwtResponseResponseEntity(ApiClientAuthRequest apiClientAuthRequest);

}
