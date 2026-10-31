package com.mb.modulith.commerce.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class MetricsCounter {

    private final MeterRegistry meterRegistry;
    private Map<String, Counter> counters;

    public void increment(String metricName, String... tags) {
        String key = createKey(metricName, tags);

        Counter counter = counters.get(key);

        if (Objects.isNull(counter)) {
            counter = Counter.builder(metricName)
                    .tags(tags)
                    .register(meterRegistry);

            counters.put(key, counter);
        }
        counter.increment();
    }

    public String createKey(String metricName, String... tags) {
        if (Objects.isNull(tags)) {
            return metricName;
        }

        String joinedKeys = String.join(".", tags);
        return String.join(".", metricName, joinedKeys);
    }

}
