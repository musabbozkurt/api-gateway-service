package com.mb.inventorymanagementservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCategoryRequest {

    @Schema(description = "Category name", example = "Product Category")
    private String name;

    @Schema(description = "Category description", example = "Product Category Description")
    private String description;
}
