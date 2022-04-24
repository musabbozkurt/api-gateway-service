package com.mb.paymentservice.exception;

public class PaymentServiceException extends BaseException {

    public PaymentServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public PaymentServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PaymentServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
