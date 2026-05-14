package com.mb.brokerageprovider.exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.http.HttpStatus;

@JsonDeserialize(as = BrokerageProviderErrorCode.class)
public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getCode();
}