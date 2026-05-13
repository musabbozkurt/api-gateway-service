package com.mb.stockexchangeservice.exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.http.HttpStatus;

@JsonDeserialize(as = StockExchangeServiceErrorCode.class)
public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getCode();
}
