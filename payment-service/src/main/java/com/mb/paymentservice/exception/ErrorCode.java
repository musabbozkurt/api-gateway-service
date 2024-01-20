package com.mb.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Arrays;

@Getter
public enum ErrorCode implements Serializable, IErrorCode {

    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    CONVERSION_FAILED(HttpStatus.BAD_REQUEST),
    MAX_SIZE(HttpStatus.BAD_REQUEST),
    MULTIPART_EXPECTED(HttpStatus.BAD_REQUEST),
    NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),
    INVALID_OR_MISSING_BODY(HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    NO_HANDLER(HttpStatus.NOT_FOUND),
    MISSING_HEADER(HttpStatus.BAD_REQUEST),
    MISSING_COOKIE(HttpStatus.BAD_REQUEST),
    MISSING_MATRIX_VARIABLE(HttpStatus.BAD_REQUEST),
    MISSING_PART(HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    VERIFICATION_TRY_EXCEED(HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;

    private String message;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static ErrorCode identifyErrorCode(String responseString) {
        return Arrays.stream(values())
                .filter(paymentsErrorCode -> responseString != null && responseString.contains(paymentsErrorCode.getCode()))
                .map(e -> e == NO_HANDLER ? METHOD_NOT_ALLOWED : e)
                .findFirst()
                .orElse(UNKNOWN_ERROR);
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
