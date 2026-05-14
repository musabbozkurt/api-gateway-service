package com.mb.brokerageprovider.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStockResponse {

    @Schema(description = "Stock id")
    private Long id;

    @Schema(description = "Stock creation date")
    private OffsetDateTime createdDateTime;

    @Schema(description = "Stock modified date")
    private OffsetDateTime modifiedDateTime;

    @Schema(description = "Product code")
    private String productCode;

    @Schema(description = "Stock quantity")
    private Long quantity;
}
