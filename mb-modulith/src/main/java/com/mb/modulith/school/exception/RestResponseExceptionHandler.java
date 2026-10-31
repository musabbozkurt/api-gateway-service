package com.mb.modulith.school.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RequiredArgsConstructor
@Component("schoolRestResponseExceptionHandler")
@ControllerAdvice(basePackages = "com.mb.modulith.school")
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(Exception ex) {
        log.error("Exception occurred. handleTypeMismatchException - ex: {}.", ExceptionUtils.getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage(), ex.getMessage()));
    }
}
