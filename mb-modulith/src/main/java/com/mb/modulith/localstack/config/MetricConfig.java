package com.mb.modulith.localstack.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {

    @Bean
    MeterRegistryCustomizer<StatsdMeterRegistry> statsdPrefix() {
        return registry -> registry
                .config()
                .meterFilter(new MeterFilter() {
                    @Override
                    public Meter.@NonNull Id map(Meter.@NonNull Id id) {
                        return id.withName("datadogPrefix" + "." + id.getName());
                    }
                });
    }
}
