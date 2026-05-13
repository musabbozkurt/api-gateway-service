package com.mb.stockexchangeservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
public enum StockExchangeServiceErrorCode implements Serializable, ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_VALUE(HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    BAD_CREDENTIALS(HttpStatus.FORBIDDEN),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(HttpStatus.FORBIDDEN),
    INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(HttpStatus.BAD_REQUEST),
    STOCK_EXCHANGE_NOT_FOUND(HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST),
    CANNOT_MAP_RESPONSE(HttpStatus.BAD_REQUEST),
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.BAD_REQUEST),
    PESSIMISTIC_LOCKING_FAILURE(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;

    StockExchangeServiceErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
