package com.mb.paymentservice.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
@Slf4j
public final class ExceptionMessageTemplateParser {
    private final Pattern pattern = Pattern.compile("(?<!\\\\)(\\{[^}^{]*})");
    private final MessageSource messageSource;

    public String interpolate(ErrorMessage errorMessage) {
        try {
            String template = getMessage(errorMessage.getErrorCode(), errorMessage.getDefaultMessage());
            return parse(template, errorMessage.getArguments());
        } catch (Exception ex) {
            log.error("Exception occurred while interpolate a message. interpolate - ex: {}.", ExceptionUtils.getStackTrace(ex));
            return null;
        }
    }

    private String parse(String template, List<Argument> arguments) {
        if (template != null && arguments != null && !arguments.isEmpty()) {
            Matcher matcher = this.pattern.matcher(template);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String placeholder = matcher.group();
                Object value = this.extractValue(placeholder, arguments);
                if (value != null) {
                    matcher.appendReplacement(sb, value.toString());
                }
            }

            matcher.appendTail(sb);
            return sb.toString().replace("\\{", "{").replace("\\}", "}");
        } else {
            return template;
        }
    }

    private String getMessage(String code, String defaultMessage) {
        try {
            return this.messageSource.getMessage(String.format("error.%s", code), null, Locale.ENGLISH);
        } catch (NoSuchMessageException var5) {
            return defaultMessage;
        }
    }

    private Object extractValue(String placeholder, List<Argument> arguments) {
        String variable = this.getPlaceholderVariable(placeholder);
        Optional<Argument> argument = arguments.stream()
                .filter(a -> a.getName().equals(variable))
                .findFirst();
        if (argument.isPresent()) {
            return argument.map(this::argumentValue).get();
        } else {
            try {
                int index = Integer.parseInt(variable);
                return this.argumentValue(arguments.get(index));
            } catch (Exception var6) {
                return null;
            }
        }
    }

    private String getPlaceholderVariable(String placeholder) {
        return placeholder.substring(1, placeholder.length() - 1);
    }

    private Object argumentValue(Argument argument) {
        Object value = argument.getValue();
        return value == null ? "null" : value;
    }
}
