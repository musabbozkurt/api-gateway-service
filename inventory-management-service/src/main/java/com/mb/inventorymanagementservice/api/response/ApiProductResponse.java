package com.mb.inventorymanagementservice.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javamoney.moneta.Money;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiProductResponse {

    @Schema(description = "Product id")
    private Long id;

    @Schema(description = "Product creation date")
    private OffsetDateTime createdDateTime;

    @Schema(description = "Product modified date")
    private OffsetDateTime modifiedDateTime;

    @Schema(description = "Product name")
    private String name;

    @Schema(description = "Product code")
    private String productCode;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Product price")
    private Money price;

    @Schema(description = "Product quantity")
    private int quantity;
}
