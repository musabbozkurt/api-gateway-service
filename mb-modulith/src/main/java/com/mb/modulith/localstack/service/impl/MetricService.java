package com.mb.modulith.localstack.service.impl;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MeterRegistry meterRegistry;

    public void count(String metricName) {
        meterRegistry.counter(metricName).increment();
    }

    public void recordTime(String tagValue, Long timeInMillis) {
        Timer.builder("example_timer")
                .tags(List.of(Tag.of("tag_key", tagValue)))
                .publishPercentiles(0.95, 0.99) // percentiles will be tagged with `phi` tag
                .register(meterRegistry)
                .record(timeInMillis, TimeUnit.MILLISECONDS);
    }

    public void count(String metricName, List<?> list) {
        Gauge.builder(metricName, list, List::size).register(meterRegistry);
    }

    public void count(String metricName, int size, String... tags) {
        meterRegistry.counter(metricName, tags).increment(size);
    }
}
