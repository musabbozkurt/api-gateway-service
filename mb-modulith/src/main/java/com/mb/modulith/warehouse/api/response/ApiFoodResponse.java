package com.mb.modulith.warehouse.api.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ApiFoodResponse {

    private int id;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime modifiedDateTime;

    private boolean deleted;

    private String name;

    private BigDecimal weight;
}
