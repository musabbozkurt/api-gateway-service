package com.mb.notificationservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageUtilsTest {

    private static final Locale TR_LOCALE = Locale.forLanguageTag("tr");

    static Stream<Arguments> englishMessages() {
        return Stream.of(
                Arguments.of("UNEXPECTED_ERROR", "An unexpected error occurred"),
                Arguments.of("VALIDATION_ERROR", "Validation error"),
                Arguments.of("ALGORITHM_ERROR", "Algorithm error"),
                Arguments.of("DUMMY_SMS_ERROR", "DummySms service error"),
                Arguments.of("MESSAGE_CAN_NOT_BE_EMPTY", "Message cannot be empty"),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND", "Notification template not found"),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE", "Notification template not found or inactive"),
                Arguments.of("NOTIFICATION_TEMPLATE_CODE_EXISTS", "A notification template with this code already exists"),
                Arguments.of("NOTIFICATION_NOT_FOUND", "Notification not found")
        );
    }

    static Stream<Arguments> turkishMessages() {
        return Stream.of(
                Arguments.of("UNEXPECTED_ERROR", "Beklenmeyen bir hata olu\u015Ftu"),
                Arguments.of("VALIDATION_ERROR", "Do\u011Frulama hatas\u0131"),
                Arguments.of("ALGORITHM_ERROR", "Algoritma hatas\u0131"),
                Arguments.of("DUMMY_SMS_ERROR", "DummySms servis hatas\u0131"),
                Arguments.of("MESSAGE_CAN_NOT_BE_EMPTY", "Mesaj bo\u015F olamaz"),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND", "Bildirim \u015Fablonu bulunamad\u0131"),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE", "Bildirim \u015Fablonu bulunamad\u0131 veya aktif de\u011Fil"),
                Arguments.of("NOTIFICATION_TEMPLATE_CODE_EXISTS", "Bu kodla bir bildirim \u015Fablonu zaten mevcut"),
                Arguments.of("NOTIFICATION_NOT_FOUND", "Bildirim bulunamad\u0131")
        );
    }

    @ParameterizedTest(name = "EN - {0} = \"{1}\"")
    @MethodSource("englishMessages")
    void getMessageFromBundle_shouldReturnEnglishMessage(String key, String expected) {
        assertEquals(expected, MessageUtils.getMessageFromBundle(key, Locale.ENGLISH));
    }

    @ParameterizedTest(name = "TR - {0} = \"{1}\"")
    @MethodSource("turkishMessages")
    void getMessageFromBundle_shouldReturnTurkishMessage(String key, String expected) {
        assertEquals(expected, MessageUtils.getMessageFromBundle(key, TR_LOCALE));
    }

    @Test
    void getMessageFromBundle_shouldFormatWithSingleArg() {
        String result = MessageUtils.getMessageFromBundle("MESSAGE_EXCEED_MAX_LENGTH", Locale.ENGLISH, 160);
        assertEquals("Message cannot exceed 160 characters", result);
    }

    @Test
    void getMessageFromBundle_shouldFormatTurkishWithSingleArg() {
        String result = MessageUtils.getMessageFromBundle("MESSAGE_EXCEED_MAX_LENGTH", TR_LOCALE, 160);
        assertEquals("Mesaj 160 karakteri a\u015Famaz", result);
    }

    @Test
    void getMessageFromBundle_shouldFormatWithMultipleArgs() {
        String result = MessageUtils.getMessageFromBundle("INVALID_GSM", Locale.ENGLISH, 10, 12);
        assertEquals("GSM number must be between 10 and 12 characters", result);
    }

    @Test
    void getMessageFromBundle_shouldFormatTurkishWithMultipleArgs() {
        String result = MessageUtils.getMessageFromBundle("INVALID_GSM", TR_LOCALE, 10, 12);
        assertEquals("GSM numaras\u0131 10 ile 12 karakter aras\u0131nda olmal\u0131d\u0131r", result);
    }

    @Test
    void getMessageFromBundle_shouldReturnKey_whenKeyNotFound() {
        String result = MessageUtils.getMessageFromBundle("NON_EXISTENT_KEY", Locale.ENGLISH);
        assertEquals("NON_EXISTENT_KEY", result);
    }

    @Test
    void getMessageFromBundle_shouldFallbackToEnglish_whenLocaleNotFound() {
        Locale japaneseLocale = Locale.JAPANESE;
        String result = MessageUtils.getMessageFromBundle("ALGORITHM_ERROR", japaneseLocale);
        assertEquals("Algorithm error", result);
    }

    @Test
    void getMessage_shouldFallbackToBundle_whenMessageSourceIsNull() {
        String result = MessageUtils.getMessage("ALGORITHM_ERROR", Locale.ENGLISH);
        assertEquals("Algorithm error", result);
    }

    @Test
    void getMessage_shouldFallbackToBundle_withArgs_whenMessageSourceIsNull() {
        String result = MessageUtils.getMessage("INVALID_GSM", Locale.ENGLISH, 10, 12);
        assertEquals("GSM number must be between 10 and 12 characters", result);
    }

    @Test
    void getMessage_shouldReturnKey_whenKeyNotFoundAndMessageSourceIsNull() {
        String result = MessageUtils.getMessage("NON_EXISTENT_KEY", Locale.ENGLISH);
        assertEquals("NON_EXISTENT_KEY", result);
    }
}
