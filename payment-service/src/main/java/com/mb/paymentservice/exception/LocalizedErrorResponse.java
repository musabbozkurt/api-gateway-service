package com.mb.paymentservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Objects;

@Slf4j
@Getter
@EqualsAndHashCode
public class LocalizedErrorResponse extends ErrorResponse {

    private static final String PREFIX = "error.%s";

    private static MessageSourceAccessor messages;

    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorDetail errorDetail;

    public LocalizedErrorResponse(String errorCode, String message) {
        super(errorCode, Objects.nonNull(getMessage(errorCode)) ? getMessage(errorCode) : message);
    }

    public LocalizedErrorResponse(String errorCode, String message, ErrorDetail errorDetail) {
        super(errorCode, Objects.nonNull(getMessage(errorCode)) ? getMessage(errorCode) : message);
        this.errorDetail = errorDetail;
    }

    private static String getMessage(String errorCode) {
        messageSourceAccessor();
        try {
            return messages.getMessage(String.format(PREFIX, errorCode));
        } catch (Exception ex) {
            log.warn("No error message defined for the errorCode: {}", errorCode);
            return null;
        }
    }

    private static MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }


    private static void messageSourceAccessor() {
        if (Objects.isNull(messages)) {
            messages = new MessageSourceAccessor(messageSource());
        }
    }

}
