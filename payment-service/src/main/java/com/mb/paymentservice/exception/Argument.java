package com.mb.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Argument {
    private final String name;
    private final Object value;
}
