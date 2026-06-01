package com.mb.apigateway.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.apigateway.constant.GatewayServiceConstants;
import com.mb.apigateway.service.ServiceAccessCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAccessCacheServiceImpl implements ServiceAccessCacheService {

    private static final TypeReference<List<ServiceAccessEntry>> SERVICE_ACCESS_ENTRIES = new TypeReference<>() {
    };

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Boolean> hasAccess(String clientId, String serviceName, String api, String method, String accessType) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(serviceName)) {
            return Mono.just(false);
        }

        return reactiveStringRedisTemplate.opsForValue()
                .get(GatewayServiceConstants.SERVICE_ACCESS_KEY_PREFIX + clientId)
                .map(serviceAccessEntryJson -> {
                    try {
                        List<ServiceAccessEntry> serviceAccessEntries = objectMapper.readValue(serviceAccessEntryJson, SERVICE_ACCESS_ENTRIES);
                        if (!isClientActive(serviceAccessEntries, accessType)) {
                            return false;
                        }
                        return isAccessPermitted(serviceAccessEntries, serviceName, api, method, accessType);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse service access cache for clientId: {} Exception: {}", clientId, ExceptionUtils.getStackTrace(e));
                        return true;
                    }
                })
                .defaultIfEmpty(true); // No cache entry means no restrictions configured
    }

    private boolean isClientActive(List<ServiceAccessEntry> serviceAccessEntries, String accessType) {
        return serviceAccessEntries.stream()
                .noneMatch(serviceAccessEntry -> !StringUtils.hasText(serviceAccessEntry.serviceName()) && !StringUtils.hasText(serviceAccessEntry.api()) && !StringUtils.hasText(serviceAccessEntry.method()) && accessType.equalsIgnoreCase(serviceAccessEntry.accessType()));
    }

    private boolean isAccessPermitted(List<ServiceAccessEntry> serviceAccessEntries, String serviceName, String api, String method, String accessType) {
        return serviceAccessEntries.stream()
                .filter(serviceAccessEntry -> serviceName.equals(serviceAccessEntry.serviceName()) && accessType.equals(serviceAccessEntry.accessType()))
                .anyMatch(serviceAccessEntry -> hasAccess(api, method, serviceAccessEntry));
    }

    private boolean hasAccess(String api, String method, ServiceAccessEntry serviceAccessEntry) {
        boolean hasFullAccess = !StringUtils.hasText(serviceAccessEntry.api()) && !StringUtils.hasText(serviceAccessEntry.method());
        boolean hasApiAccess = StringUtils.hasText(api) && api.equals(serviceAccessEntry.api()) && (!StringUtils.hasText(serviceAccessEntry.method()) || serviceAccessEntry.method().equalsIgnoreCase(method));
        return hasFullAccess || hasApiAccess;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceAccessEntry(String serviceName, String api, String method, String accessType) {
    }
}
