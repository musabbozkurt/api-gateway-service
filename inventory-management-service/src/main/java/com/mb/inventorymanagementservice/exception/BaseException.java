package com.mb.inventorymanagementservice.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

import static com.mb.inventorymanagementservice.utils.Constants.EMPTY_LIST;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException implements Serializable {

    private final transient ErrorCode errorCode;
    private final String message;
    private final transient List<?> params;

    public BaseException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
        this.message = null;
        this.params = EMPTY_LIST;
    }

    public BaseException(ErrorCode errorCode, List<?> params) {
        super();
        this.errorCode = errorCode;
        this.message = null;
        this.params = params;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.message = cause.getMessage();
        this.params = EMPTY_LIST;
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.params = EMPTY_LIST;
    }

    public BaseException(ErrorCode errorCode, String message, List<?> params) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.params = params;
    }

    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = StringUtils.defaultIfBlank(message, cause.getMessage());
        this.params = EMPTY_LIST;
    }
}
