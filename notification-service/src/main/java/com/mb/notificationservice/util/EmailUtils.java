package com.mb.notificationservice.util;

import com.mb.notificationservice.api.request.NotificationRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailUtils {

    public static boolean isValid(NotificationRequest request) {
        return (StringUtils.isNotBlank(request.getTemplateCode())
                || (StringUtils.isNotBlank(request.getSubject()) && StringUtils.isNotBlank(request.getBody())))
                && isValidEmailCollection(request.getRecipients(), true)
                && isValidEmailCollection(request.getCc(), false)
                && isValidEmailCollection(request.getBcc(), false);
    }

    private static boolean isValidEmailCollection(Set<String> emails, boolean required) {
        return CollectionUtils.isEmpty(emails) ? !required : emails.parallelStream().allMatch(EmailValidator.getInstance()::isValid);
    }
}
