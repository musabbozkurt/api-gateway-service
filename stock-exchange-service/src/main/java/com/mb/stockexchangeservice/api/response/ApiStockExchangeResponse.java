package com.mb.stockexchangeservice.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStockExchangeResponse {

    private Long id;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime modifiedDateTime;

    private String name;

    private String description;

    private boolean liveInMarket;

    private int version;

    private List<ApiStockResponse> stocks;
}
