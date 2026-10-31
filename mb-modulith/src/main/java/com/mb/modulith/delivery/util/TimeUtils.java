package com.mb.modulith.delivery.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {

    public static Duration deliveryDuration(Instant startTime, Instant endTime) {
        return Duration.between(startTime, endTime);
    }
}
