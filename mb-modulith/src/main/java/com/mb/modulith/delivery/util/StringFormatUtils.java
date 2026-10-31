package com.mb.modulith.delivery.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringFormatUtils {

    public static String getStringFrom(ResourceBundle bundle, String key, Object... placeholders) {
        String pattern = bundle.getString(key);
        return MessageFormat.format(pattern, placeholders);
    }
}
