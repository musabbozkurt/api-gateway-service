package com.mb.notificationservice.util;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmsUtils {

    private static final int MIN_NUMBER_LENGTH = 10;
    private static final int MAX_NUMBER_LENGTH = 12;
    private static final int MAX_MESSAGE_LENGTH = 255;

    public static void validate(NotificationRequest request) {
        if (ObjectUtils.isEmpty(request.getBody())) {
            throw new BaseException(NotificationErrorCode.MESSAGE_CAN_NOT_BE_EMPTY);
        }

        if (request.getBody().length() > MAX_MESSAGE_LENGTH) {
            throw new BaseException(NotificationErrorCode.MESSAGE_EXCEED_MAX_LENGTH, MAX_MESSAGE_LENGTH);
        }

        String gsm = StringUtils.normalizeSpace(request.getRecipients().iterator().next().replace("+", "")).replace(" ", "");
        if (gsm.length() < MIN_NUMBER_LENGTH || gsm.length() > MAX_NUMBER_LENGTH) {
            throw new BaseException(NotificationErrorCode.INVALID_GSM, MIN_NUMBER_LENGTH, MAX_NUMBER_LENGTH);
        }
    }
}
