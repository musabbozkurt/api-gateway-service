package com.mb.paymentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ValidationErrorDetail implements ErrorDetail {
    private List<ValidationError> errors;

    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
        private String errorCode;
    }
}
