package com.mb.notificationservice.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for retrieving localized messages.
 * Can be used in both Spring-managed beans and static contexts (like tests).
 */
@Component
public final class MessageUtils {

    private static final String BUNDLE_NAME = "messages";
    private static MessageSource messageSource;

    MessageUtils(MessageSource messageSource) {
        initialize(messageSource);
    }

    private static synchronized void initialize(MessageSource source) {
        messageSource = source;
    }

    public static String getMessage(String key) {
        return getMessage(key, LocaleContextHolder.getLocale());
    }

    public static String getMessage(String key, Locale locale) {
        return getMessage(key, locale, (Object[]) null);
    }

    public static String getMessage(String key, Locale locale, Object... args) {
        Object[] effectiveArgs = (args != null && args.length > 0) ? args : null;
        if (messageSource != null) {
            return messageSource.getMessage(key, effectiveArgs, key, locale);
        }
        return getMessageFromBundle(key, locale, effectiveArgs);
    }

    public static String getMessageFromBundle(String key, Locale locale) {
        return getMessageFromBundle(key, locale, (Object[]) null);
    }

    public static String getMessageFromBundle(String key, Locale locale, Object... args) {
        try {
            String pattern = ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
            if (args != null && args.length > 0) {
                return MessageFormat.format(pattern, args);
            }
            return pattern;
        } catch (MissingResourceException _) {
            try {
                String pattern = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH).getString(key);
                if (args != null && args.length > 0) {
                    return MessageFormat.format(pattern, args);
                }
                return pattern;
            } catch (MissingResourceException _) {
                return key;
            }
        }
    }
}
