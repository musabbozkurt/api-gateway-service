package com.mb.notificationservice.exception;

import com.mb.notificationservice.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        String errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new ErrorResponse(NotificationErrorCode.VALIDATION_ERROR.getCode(), errorMessages), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.debug("BaseException occurred: {}", ex.getErrorCode(), ex);
        String code = ex.getErrorCode().getCode();
        Locale locale = LocaleContextHolder.getLocale();
        String message = MessageUtils.getMessage(code, locale, ex.getArgs());

        if (code.equals(message) && ex.getMessage() != null) {
            message = ex.getMessage();
        }

        return new ResponseEntity<>(new ErrorResponse(code, message), ex.getErrorCode().getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.debug("Exception occurred: {}", ExceptionUtils.getStackTrace(ex));
        Locale locale = LocaleContextHolder.getLocale();
        String code = NotificationErrorCode.UNEXPECTED_ERROR.getCode();
        String message = MessageUtils.getMessage(code, locale);
        return new ResponseEntity<>(new ErrorResponse(code, message), NotificationErrorCode.UNEXPECTED_ERROR.getHttpStatus());
    }
}
