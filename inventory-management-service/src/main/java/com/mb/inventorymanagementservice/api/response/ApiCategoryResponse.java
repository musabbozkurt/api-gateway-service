package com.mb.inventorymanagementservice.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * {@code @JsonCreator} on the no-args constructor forces Jackson to use setter-based
 * deserialization instead of the all-args constructor. Without it, Jackson 3.x picks
 * the all-args constructor and fails with {@code MismatchedInputException} when a
 * primitive field (e.g. {@code liveInMarket}, {@code version}) is absent from JSON,
 * because {@code null} cannot be mapped to a primitive type.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @JsonCreator)
public class ApiCategoryResponse {

    private Long id;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime modifiedDateTime;

    private String name;

    private String description;

    private boolean liveInMarket;

    private int version;

    private List<ApiProductResponse> products;
}
