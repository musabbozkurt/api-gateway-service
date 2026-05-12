package com.mb.brokerageprovider.config.aspect;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(999)
public class LogObservationHandler implements ObservationHandler<Observation.Context> {

    @Override
    public void onScopeOpened(Observation.Context context) {
        context.put("time", System.currentTimeMillis());
        log.info("LogObservationHandler::onScopeOpened - Execution started. contextName: {}", context.getName());
    }

    @Override
    public void onScopeClosed(Observation.Context context) {
        log.info("LogObservationHandler::onScopeClosed - Execution stopped. Name: {} Duration: {} ms", context.getName(), (System.currentTimeMillis() - context.getOrDefault("time", 0L)));
    }

    @Override
    public boolean supportsContext(@NonNull Observation.Context context) {
        return true;
    }
}
