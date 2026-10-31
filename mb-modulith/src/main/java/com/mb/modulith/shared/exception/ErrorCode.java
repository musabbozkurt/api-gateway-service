package com.mb.modulith.shared.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

    String getCode();

    HttpStatus getHttpStatus();

    String getMessage();
}
