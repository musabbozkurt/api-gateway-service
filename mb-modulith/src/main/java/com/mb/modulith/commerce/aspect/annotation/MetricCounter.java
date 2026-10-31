package com.mb.modulith.commerce.aspect.annotation;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Repeatable(MetricCounters.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricCounter {

    MetricType metricType();

    MetricSubType metricSubType() default MetricSubType.DEFAULT;

    @Getter
    enum MetricType {
        CLIENT("client.creation.failed"),
        CLIENT_NOT_FOUND("client.search.failed");

        private final String metricName;

        MetricType(String metricName) {
            this.metricName = metricName;
        }
    }

    @Getter
    enum MetricSubType {
        DEFAULT("default"),
        CLIENT_CREATED("created"),
        CLIENT_INITIATED("initiated");

        private final String metricSubTypeName;

        MetricSubType(String metricSubTypeName) {
            this.metricSubTypeName = metricSubTypeName;
        }
    }
}
