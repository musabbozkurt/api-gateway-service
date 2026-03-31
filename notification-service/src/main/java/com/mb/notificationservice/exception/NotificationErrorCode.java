package com.mb.notificationservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
public enum NotificationErrorCode implements Serializable, ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    ALGORITHM_ERROR(HttpStatus.BAD_REQUEST),
    DUMMY_SMS_ERROR(HttpStatus.BAD_REQUEST),
    MESSAGE_EXCEED_MAX_LENGTH(HttpStatus.BAD_REQUEST),
    MESSAGE_CAN_NOT_BE_EMPTY(HttpStatus.BAD_REQUEST),
    INVALID_GSM(HttpStatus.BAD_REQUEST),
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND),
    NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE(HttpStatus.NOT_FOUND),
    NOTIFICATION_TEMPLATE_CODE_EXISTS(HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND),
    UNSUPPORTED_NOTIFICATION_CHANNEL(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;

    NotificationErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
