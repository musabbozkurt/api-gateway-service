package com.mb.stockexchangeservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStockExchangeRequest {

    @Schema(description = "Stock exchange name", example = "New York Stock Exchange")
    private String name;

    @Schema(description = "Stock exchange description", example = "New York Stock Exchange")
    private String description;
}
