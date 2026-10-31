package com.mb.modulith.clinic.exception;

import com.mb.modulith.shared.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Component("clinicRestResponseExceptionHandler")
@ControllerAdvice(basePackages = "com.mb.modulith.clinic")
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.debug("BaseException occurred: {}", ex.getErrorCode(), ex);
        var errorCode = ex.getErrorCode();
        return new ResponseEntity<>(
                new ErrorResponse(errorCode.getCode(), ex.getMessage()),
                errorCode.getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.debug("Exception occurred: {}", ExceptionUtils.getStackTrace(ex));
        return new ResponseEntity<>(
                new ErrorResponse(ClinicServiceErrorCode.UNEXPECTED_ERROR.getCode(), ex.getMessage()),
                ClinicServiceErrorCode.UNEXPECTED_ERROR.getHttpStatus());
    }
}
