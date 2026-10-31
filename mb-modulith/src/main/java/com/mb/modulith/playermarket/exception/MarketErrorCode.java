package com.mb.modulith.playermarket.exception;

import com.mb.modulith.shared.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum MarketErrorCode implements ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    PLAYER_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST),
    TEAM_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST),
    PLAYER_ALREADY_HAS_TEAM_EXCEPTION(HttpStatus.BAD_REQUEST),
    TEAM_COMMISSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST);

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String message;

    MarketErrorCode(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    MarketErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
