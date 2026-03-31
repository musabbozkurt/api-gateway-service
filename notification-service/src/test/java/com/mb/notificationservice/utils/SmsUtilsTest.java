package com.mb.notificationservice.utils;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.util.SmsUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmsUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"5321234567", "05321234567", "905321234567", "+905321234567"})
    void validate_ShouldNotThrow_WhenGsmIsValid(String gsm) {
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of(gsm));

        assertDoesNotThrow(() -> SmsUtils.validate(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_ShouldThrowMessageCanNotBeEmpty_WhenBodyIsNullOrEmpty(String body) {
        NotificationRequest request = createValidRequest();
        request.setBody(body);

        BaseException exception = assertThrows(BaseException.class, () -> SmsUtils.validate(request));
        assertEquals(NotificationErrorCode.MESSAGE_CAN_NOT_BE_EMPTY, exception.getErrorCode());
    }

    @Test
    void validate_ShouldThrowMessageExceedMaxLength_WhenBodyExceeds255Characters() {
        NotificationRequest request = createValidRequest();
        request.setBody("a".repeat(256));

        BaseException exception = assertThrows(BaseException.class, () -> SmsUtils.validate(request));
        assertEquals(NotificationErrorCode.MESSAGE_EXCEED_MAX_LENGTH, exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "123456789", "1234567890123"})
    void validate_ShouldThrowInvalidGsm_WhenGsmLengthIsInvalid(String gsm) {
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of(gsm));

        BaseException exception = assertThrows(BaseException.class, () -> SmsUtils.validate(request));
        assertEquals(NotificationErrorCode.INVALID_GSM, exception.getErrorCode());
    }

    private NotificationRequest createValidRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setRecipients(Set.of("5321234567"));
        request.setBody("Test SMS message");
        return request;
    }
}
