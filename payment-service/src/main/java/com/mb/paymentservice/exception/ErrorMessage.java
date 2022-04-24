package com.mb.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ErrorMessage {
    private final String errorCode;
    private final List<Argument> arguments;
    private final String defaultMessage;
}
