package com.mb.notificationservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentUtils {

    public static boolean isHtml(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        String trimmed = content.trim().toLowerCase();
        return trimmed.startsWith("<!doctype html")
                || trimmed.startsWith("<html")
                || trimmed.contains("<body")
                || trimmed.contains("<table")
                || trimmed.contains("<div")
                || trimmed.contains("th:")
                || trimmed.contains("[[${");
    }
}
