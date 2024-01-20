package com.mb.paymentservice.exception;

import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class BaseException extends RuntimeException implements Serializable {
    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
