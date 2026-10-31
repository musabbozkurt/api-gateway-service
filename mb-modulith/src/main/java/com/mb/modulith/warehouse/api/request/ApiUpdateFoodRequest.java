package com.mb.modulith.warehouse.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiUpdateFoodRequest {

    private String name;
    private BigDecimal weight;
}
