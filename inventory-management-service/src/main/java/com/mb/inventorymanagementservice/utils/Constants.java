package com.mb.inventorymanagementservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Collected constants of general utility. All members of this class are immutable.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String DEFAULT_ID_GENERATOR_NAME = "default_sequence_generator";
    public static final List<?> EMPTY_LIST = List.of();
    private static final List<String> EXCLUDED_URIS = List.of("/actuator/prometheus");

    public static boolean isUriExcluded(String uri) {
        return EXCLUDED_URIS.stream().anyMatch(uri::matches);
    }
}
