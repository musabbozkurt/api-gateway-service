package com.mb.modulith.commerce.exception.handle;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Component("commerceApiExceptionHandler")
@RestControllerAdvice(basePackages = "com.mb.modulith.commerce")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static SharedErrorCode resolveCommerceErrorCode(BaseException ex) {
        if (ex.getErrorCode() instanceof SharedErrorCode code) {
            return code;
        }
        return SharedErrorCode.UNEXPECTED_ERROR;
    }

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiError> handleBaseException(BaseException ex) {
        SharedErrorCode sharedErrorCode = resolveCommerceErrorCode(ex);
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(sharedErrorCode.getHttpStatus().value())
                .sharedErrorCode(sharedErrorCode)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(SharedErrorCode.CONFLICT.getHttpStatus().value())
                .sharedErrorCode(SharedErrorCode.CONFLICT)
                .message(ex.getMessage())
                .build());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiError> handleInternalServerError(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(SharedErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .sharedErrorCode(SharedErrorCode.INTERNAL_SERVER_ERROR)
                .message(ex.getMessage())
                .build());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<ApiError> handleEntityNotFound(RuntimeException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(SharedErrorCode.NOT_FOUND.getHttpStatus().value())
                .sharedErrorCode(SharedErrorCode.NOT_FOUND)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains(String.valueOf(SharedErrorCode.BAD_CREDENTIALS.getHttpStatus().value()))) {
            return ResponseEntity.ok(ApiError.builder()
                    .httpStatus(SharedErrorCode.BAD_CREDENTIALS.getHttpStatus().value())
                    .sharedErrorCode(SharedErrorCode.BAD_CREDENTIALS)
                    .message(ex.getMessage())
                    .build());
        } else if (ExceptionUtils.getStackTrace(ex).contains("org.springframework.security.access.AccessDeniedException")) {
            return ResponseEntity.ok(ApiError.builder()
                    .httpStatus(SharedErrorCode.ACCESS_DENIED.getHttpStatus().value())
                    .sharedErrorCode(SharedErrorCode.ACCESS_DENIED)
                    .message(ex.getMessage())
                    .build());
        } else {
            SharedErrorCode sharedErrorCode = SharedErrorCode.getErrorCode(ExceptionUtils.getStackTrace(ex));
            return ResponseEntity.ok(ApiError.builder()
                    .httpStatus(sharedErrorCode.getHttpStatus().value())
                    .sharedErrorCode(sharedErrorCode)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ApiError> handleUsernameNotFoundException(BadCredentialsException ex) {
        return ResponseEntity.ok(ApiError.builder()
                .httpStatus(SharedErrorCode.BAD_CREDENTIALS.getHttpStatus().value())
                .sharedErrorCode(SharedErrorCode.BAD_CREDENTIALS)
                .message(ex.getMessage())
                .build());
    }
}
