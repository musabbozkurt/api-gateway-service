package com.mb.stockexchangeservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStockRequest {

    @Schema(description = "Stock name", example = "AAPL")
    private String name;

    @Schema(description = "Stock description", example = "APPLE")
    private String description;

    @Schema(description = "Stock current price", example = "10.32")
    private BigDecimal currentPrice;
}
