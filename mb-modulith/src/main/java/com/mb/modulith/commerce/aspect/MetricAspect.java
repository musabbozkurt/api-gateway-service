package com.mb.modulith.commerce.aspect;

import com.mb.modulith.commerce.aspect.annotation.MetricCounter;
import com.mb.modulith.commerce.aspect.annotation.MetricCounters;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.service.impl.MetricsCounter;
import com.mb.modulith.shared.exception.BaseException;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mb.modulith.commerce.aspect.annotation.MetricCounter.MetricType.CLIENT;
import static com.mb.modulith.commerce.aspect.annotation.MetricCounter.MetricType.CLIENT_NOT_FOUND;

@Aspect
@Component
@AllArgsConstructor
public class MetricAspect {
    private static final String UNKNOWN = "unknown";
    private static final String METRIC_TAG_STATE = "state";
    private static final String METRIC_TAG_ACTION = "action";
    private static final String METRIC_TAG_VALUE_SENT = "SENT";
    private static final String METRIC_TAG_VALUE_VERIFIED = "CHECKED";
    private static final String METRIC_TAG_REASON = "reason";

    private final MetricsCounter metricsCounter;

    @AfterThrowing(value = "anyMetricCounter() || anyMetricCounters()", throwing = "ex")
    public void onExceptionOccurs(JoinPoint joinPoint, Throwable ex) {
        getMetricTypes(joinPoint).forEach(metricType -> {
            if (metricType == CLIENT || (metricType == CLIENT_NOT_FOUND && isClientNotFoundException(ex))) {
                applyClientMetric(metricType, joinPoint, ex);
            }
        });
    }

    @After(value = "anyMetricCounter() || anyMetricCounters()")
    public void onMethodCompleted(JoinPoint joinPoint) {
        getMetricTypes(joinPoint).forEach(metricType -> {
            if (metricType == CLIENT_NOT_FOUND) {
                applyClientNotFoundMetric(metricType);
            } else if (metricType == CLIENT) {
                getMetricSubTypes(joinPoint).forEach(metricSubType -> {
                    String state = getState(joinPoint);
                    metricsCounter.increment(metricType.getMetricName(), metricSubType.getMetricSubTypeName(), state);
                });
            }
        });
    }

    private List<MetricCounter.MetricType> getMetricTypes(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        MetricCounter metricCounter = methodSignature.getMethod().getAnnotation(MetricCounter.class);
        if (metricCounter == null) {
            return Arrays.stream(methodSignature.getMethod().getAnnotation(MetricCounters.class).value())
                    .map(MetricCounter::metricType)
                    .toList();
        }
        return Collections.singletonList(metricCounter.metricType());
    }

    private void applyClientNotFoundMetric(MetricCounter.MetricType metricType) {
        String action = metricType == CLIENT_NOT_FOUND ? METRIC_TAG_VALUE_SENT : METRIC_TAG_VALUE_VERIFIED;
        metricsCounter.increment(metricType.getMetricName(), METRIC_TAG_ACTION, action);
    }

    private void applyClientMetric(MetricCounter.MetricType metricType, JoinPoint joinPoint, Throwable ex) {
        if (Objects.isNull(joinPoint.getArgs()))
            return;

        // state can be status of the Order or status of something else
        String state = getState(joinPoint);

        String reason = ex instanceof Exception ? ex.getMessage() : UNKNOWN;
        metricsCounter.increment(metricType.getMetricName(), METRIC_TAG_STATE, state, METRIC_TAG_REASON, reason);
    }

    private String getState(JoinPoint joinPoint) {
        Optional<Client> opt = Arrays.stream(joinPoint.getArgs())
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .findFirst();

        return opt.map(Client::getTelephone).orElse(UNKNOWN);
    }

    private boolean isClientNotFoundException(Throwable ex) {
        if (ex instanceof BaseException baseException
                && baseException.getErrorCode() instanceof SharedErrorCode sharedErrorCode) {
            return sharedErrorCode == SharedErrorCode.CLIENT_NOT_FOUND;
        }
        return false;
    }

    private List<MetricCounter.MetricSubType> getMetricSubTypes(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        MetricCounter metricCounter = methodSignature.getMethod().getAnnotation(MetricCounter.class);
        if (metricCounter == null) {
            return Arrays.stream(methodSignature.getMethod().getAnnotation(MetricCounters.class).value())
                    .map(MetricCounter::metricSubType)
                    .filter(metricSubType -> metricSubType != MetricCounter.MetricSubType.DEFAULT)
                    .toList();
        }
        return Collections.singletonList(metricCounter.metricSubType());
    }

    @Pointcut("@annotation(com.mb.modulith.commerce.aspect.annotation.MetricCounter)")
    public void anyMetricCounter() {
    }

    @Pointcut("@annotation(com.mb.modulith.commerce.aspect.annotation.MetricCounters)")
    public void anyMetricCounters() {
    }
}
