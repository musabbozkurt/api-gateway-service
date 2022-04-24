package com.mb.paymentservice.exception;

import org.springframework.http.HttpStatus;

public interface IErrorCode {
    HttpStatus getHttpStatus();

    String getCode();
}
