package com.mb.apigateway.logging;

import com.mb.apigateway.enums.ActivityStatus;

import java.time.Instant;

public record UserActivityEvent(ActivityStatus status,
                                String requestId,
                                String serviceName,
                                String api,
                                String method,
                                Instant startedAt,
                                Instant finishedAt,
                                Long durationMs,
                                Integer httpStatus,
                                String errorMessage) {
}
