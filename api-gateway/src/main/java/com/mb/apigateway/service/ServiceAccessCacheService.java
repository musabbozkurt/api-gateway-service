package com.mb.apigateway.service;

import reactor.core.publisher.Mono;

public interface ServiceAccessCacheService {

    Mono<Boolean> hasAccess(String clientId, String serviceName, String api, String method, String accessType);
}
