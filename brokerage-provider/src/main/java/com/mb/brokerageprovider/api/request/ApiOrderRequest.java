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
public class ApiOrderRequest {

    @Schema(description = "User id", example = "1234")
    private Long userId;

    @Schema(description = "Product code", example = "APPLE")
    private String productCode;

    @Schema(description = "Number of product", example = "3")
    private Long quantity;
}
