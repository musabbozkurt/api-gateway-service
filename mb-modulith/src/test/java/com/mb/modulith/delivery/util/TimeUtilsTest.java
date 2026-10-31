package com.mb.modulith.delivery.util;

import com.mb.modulith.delivery.base.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilsTest extends BaseUnitTest {

    @Test
    void deliveryDuration_ShouldReturnDuration_WhenStartAndEndAreSet() {
        // Arrange
        Instant startTime = Instant.parse("2020-01-01T00:00:00Z");
        Instant endTime = Instant.parse("2020-01-01T01:30:00Z");

        // Act
        Duration result = TimeUtils.deliveryDuration(startTime, endTime);

        // Assertions
        assertEquals(Duration.ofMinutes(90), result);
    }
}
