package com.mb.inventorymanagementservice.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static com.mb.inventorymanagementservice.utils.Constants.EMPTY_LIST;

@Getter
public class LocalizedErrorResponse extends ErrorResponse {

    private static final String PREFIX = "error.%s";
    private static final String DEFAULT = "DEFAULT";

    private static MessageSourceAccessor messages;

    public LocalizedErrorResponse(String errorCode) {
        super(errorCode, getMessage(errorCode, EMPTY_LIST));
    }

    public LocalizedErrorResponse(String errorCode, List<?> args) {
        super(errorCode, getMessage(errorCode, args), args);
    }

    public LocalizedErrorResponse(String errorCode, String message) {
        super(errorCode, StringUtils.isNotBlank(message) ? message : getMessage(errorCode, EMPTY_LIST));
    }

    public LocalizedErrorResponse(String errorCode, String message, List<?> args) {
        super(errorCode, StringUtils.isNotBlank(message) ? message : getMessage(errorCode, args), args);
    }

    private static String getMessage(String errorCode, List<?> args) {
        try {
            if (Objects.isNull(messages)) {
                messages = new MessageSourceAccessor(messageSource());
            }

            if (CollectionUtils.isEmpty(args)) {
                return messages.getMessage(String.format(PREFIX, errorCode));
            }

            return messages.getMessage(String.format(PREFIX, errorCode), args.toArray());
        } catch (Exception _) {
            return messages.getMessage(String.format(PREFIX, DEFAULT));
        }
    }

    private static MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages", "classpath:messages-default");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
