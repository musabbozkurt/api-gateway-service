package com.mb.brokerageprovider.exception;

import org.springframework.http.HttpStatus;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BrokerageProviderErrorCode.class)
public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getCode();
}
