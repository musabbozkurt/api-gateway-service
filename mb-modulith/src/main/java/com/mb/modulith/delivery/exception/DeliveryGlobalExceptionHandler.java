package com.mb.modulith.delivery.exception;

import com.mb.modulith.delivery.util.DeliveryConstants;
import com.mb.modulith.delivery.util.StringFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.AbstractMap;
import java.util.ResourceBundle;

@Slf4j
@SuppressWarnings("unused")
@Component("deliveryGlobalExceptionHandler")
@ControllerAdvice(basePackages = "com.mb.modulith.delivery")
public class DeliveryGlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<AbstractMap.SimpleEntry<String, String>> handle(Exception exception) {
        log.error("Request could not be processed: ", exception);
        ResourceBundle messages = ResourceBundle.getBundle(DeliveryConstants.MESSAGES_BASENAME, LocaleContextHolder.getLocale());
        String detail = StringFormatUtils.getStringFrom(messages, DeliveryConstants.APPLICATION_TEST_MESSAGE_KEY, exception.getMessage(), exception.getLocalizedMessage());
        AbstractMap.SimpleEntry<String, String> response = new AbstractMap.SimpleEntry<>("message", "Request could not be processed. " + detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
