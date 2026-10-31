package com.mb.modulith.email.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum EmailErrorCode implements com.mb.modulith.shared.exception.ErrorCode {
    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Email could not be found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;

    EmailErrorCode(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    EmailErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
