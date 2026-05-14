package com.mb.brokerageprovider.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStockRequest {

    @Schema(description = "Product code", example = "APPLE")
    private String productCode;

    @Schema(description = "Stock quantity", example = "10")
    private Long quantity;
}
