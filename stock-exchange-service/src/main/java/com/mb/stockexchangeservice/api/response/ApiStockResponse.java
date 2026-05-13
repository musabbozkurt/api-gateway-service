package com.mb.stockexchangeservice.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @Schema(description = "Stock name")
    private String name;

    @Schema(description = "Stock description")
    private String description;

    @Schema(description = "Stock current price")
    private BigDecimal currentPrice;
}
