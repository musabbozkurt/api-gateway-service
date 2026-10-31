package com.mb.modulith.clinic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public enum ClinicServiceErrorCode implements Serializable, ErrorCode, com.mb.modulith.shared.exception.ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_VALUE(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CANNOT_MAP_RESPONSE(HttpStatus.BAD_REQUEST);

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;

    ClinicServiceErrorCode(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    ClinicServiceErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.name();
    }

}
