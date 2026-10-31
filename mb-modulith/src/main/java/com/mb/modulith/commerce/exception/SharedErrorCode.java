package com.mb.modulith.commerce.exception;

import com.mb.modulith.shared.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public enum SharedErrorCode implements ErrorCode {
    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    CANNOT_MAP_RESPONSE(HttpStatus.BAD_REQUEST),
    INVALID_EVENT(HttpStatus.BAD_REQUEST),
    INVALID_EVENT_TYPE(HttpStatus.BAD_REQUEST),
    INVALID_EVENT_CLASS(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    CONFLICT(HttpStatus.CONFLICT),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Client not found"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    ORDER_LOCKED(HttpStatus.CONFLICT, "Order can not be changed"),
    USERNAME_ALREADY_IN_USE(HttpStatus.CONFLICT, "Username is already in use"),
    EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT, "Email is already in use"),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "Invalid Email");

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;

    SharedErrorCode(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    SharedErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public static SharedErrorCode getErrorCode(String errorCode) {
        return Arrays.stream(values())
                .filter(code -> errorCode != null && errorCode.contains(code.name()))
                .findFirst()
                .orElse(UNEXPECTED_ERROR);
    }

    @Override
    public String getCode() {
        return name();
    }
}
