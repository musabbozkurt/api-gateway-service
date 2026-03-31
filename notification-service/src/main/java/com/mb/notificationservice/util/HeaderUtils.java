package com.mb.notificationservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderUtils {

    public static final String USER_NAME = "username";
    public static final String CLIENT_ID = "client_id";
    private static final String USER_ID = "userId";

    public static String getUsername() {
        return getHeaderValue(USER_NAME)
                .filter(StringUtils::isNotBlank)
                .orElse(TokenUtils.getUsername());
    }

    public static String getClientId() {
        return getHeaderValue(CLIENT_ID)
                .filter(StringUtils::isNotBlank)
                .orElse(TokenUtils.getClientId());
    }

    public static Long getUserId() {
        return getHeaderValue(USER_ID)
                .filter(StringUtils::isNotBlank)
                .map(Long::parseLong)
                .orElse(TokenUtils.getUserId());
    }

    private static Optional<String> getHeaderValue(String header) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.ofNullable(servletRequestAttributes.getRequest().getHeader(header));
        }

        return Optional.empty();
    }
}
