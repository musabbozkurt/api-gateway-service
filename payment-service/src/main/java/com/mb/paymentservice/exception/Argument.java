package com.mb.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Argument {
    private final String name;
    private final Object value;
}
