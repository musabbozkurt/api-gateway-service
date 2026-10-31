package com.mb.modulith.email.exception.handle;

import com.mb.modulith.email.exception.EmailErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Component("emailRestExceptionHandler")
@RestControllerAdvice(basePackages = "com.mb.modulith.email")
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static EmailErrorCode resolveEmailErrorCode(BaseException ex) {
        if (ex.getErrorCode() instanceof EmailErrorCode code) {
            return code;
        }
        return EmailErrorCode.UNEXPECTED_ERROR;
    }

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiError> handleBaseException(BaseException ex) {
        EmailErrorCode errorCode = resolveEmailErrorCode(ex);
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(errorCode.getHttpStatus())
                .emailErrorCode(errorCode)
                .message(ex.getMessage())
                .build());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiError> handleInternalServerError(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(EmailErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .emailErrorCode(EmailErrorCode.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .build());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<ApiError> handleEntityNotFound(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(EmailErrorCode.NOT_FOUND.getHttpStatus())
                .emailErrorCode(EmailErrorCode.NOT_FOUND)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(EmailErrorCode.UNEXPECTED_ERROR.getHttpStatus())
                .emailErrorCode(EmailErrorCode.UNEXPECTED_ERROR)
                .message(ex.getMessage())
                .build());
    }
}
