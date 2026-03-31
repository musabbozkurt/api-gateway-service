package com.mb.notificationservice.exception;

import com.mb.notificationservice.util.MessageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestResponseExceptionHandlerTest {

    private final RestResponseExceptionHandler handler = new RestResponseExceptionHandler();

    static Stream<Arguments> notificationErrorCodes_english() {
        return Stream.of(
                Arguments.of("UNEXPECTED_ERROR", HttpStatus.BAD_REQUEST, "An unexpected error occurred"),
                Arguments.of("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation error"),
                Arguments.of("ALGORITHM_ERROR", HttpStatus.BAD_REQUEST, "Algorithm error"),
                Arguments.of("DUMMY_SMS_ERROR", HttpStatus.BAD_REQUEST, "DummySms service error"),
                Arguments.of("MESSAGE_EXCEED_MAX_LENGTH", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("MESSAGE_EXCEED_MAX_LENGTH", Locale.ENGLISH)),
                Arguments.of("MESSAGE_CAN_NOT_BE_EMPTY", HttpStatus.BAD_REQUEST, "Message cannot be empty"),
                Arguments.of("INVALID_GSM", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("INVALID_GSM", Locale.ENGLISH)),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND", HttpStatus.NOT_FOUND, "Notification template not found"),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE", HttpStatus.NOT_FOUND, "Notification template not found or inactive"),
                Arguments.of("NOTIFICATION_TEMPLATE_CODE_EXISTS", HttpStatus.BAD_REQUEST, "A notification template with this code already exists"),
                Arguments.of("NOTIFICATION_NOT_FOUND", HttpStatus.NOT_FOUND, "Notification not found"),
                Arguments.of("UNSUPPORTED_NOTIFICATION_CHANNEL", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("UNSUPPORTED_NOTIFICATION_CHANNEL", Locale.ENGLISH))
        );
    }

    static Stream<Arguments> notificationErrorCodes_turkish() {
        Locale trLocale = Locale.forLanguageTag("tr");
        return Stream.of(
                Arguments.of("UNEXPECTED_ERROR", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("UNEXPECTED_ERROR", trLocale)),
                Arguments.of("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("VALIDATION_ERROR", trLocale)),
                Arguments.of("ALGORITHM_ERROR", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("ALGORITHM_ERROR", trLocale)),
                Arguments.of("DUMMY_SMS_ERROR", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("DUMMY_SMS_ERROR", trLocale)),
                Arguments.of("MESSAGE_EXCEED_MAX_LENGTH", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("MESSAGE_EXCEED_MAX_LENGTH", trLocale)),
                Arguments.of("MESSAGE_CAN_NOT_BE_EMPTY", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("MESSAGE_CAN_NOT_BE_EMPTY", trLocale)),
                Arguments.of("INVALID_GSM", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("INVALID_GSM", trLocale)),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND", HttpStatus.NOT_FOUND, MessageUtils.getMessageFromBundle("NOTIFICATION_TEMPLATE_NOT_FOUND", trLocale)),
                Arguments.of("NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE", HttpStatus.NOT_FOUND, MessageUtils.getMessageFromBundle("NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE", trLocale)),
                Arguments.of("NOTIFICATION_TEMPLATE_CODE_EXISTS", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("NOTIFICATION_TEMPLATE_CODE_EXISTS", trLocale)),
                Arguments.of("NOTIFICATION_NOT_FOUND", HttpStatus.NOT_FOUND, MessageUtils.getMessageFromBundle("NOTIFICATION_NOT_FOUND", trLocale)),
                Arguments.of("UNSUPPORTED_NOTIFICATION_CHANNEL", HttpStatus.BAD_REQUEST, MessageUtils.getMessageFromBundle("UNSUPPORTED_NOTIFICATION_CHANNEL", trLocale))
        );
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @MethodSource("notificationErrorCodes_english")
    @ParameterizedTest(name = "EN - {0} should return \"{2}\"")
    void handleBaseException_shouldReturnEnglishMessage(String code, HttpStatus httpStatus, String expectedMessage) {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        BaseException ex = new BaseException(createErrorCode(code, httpStatus));

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals(httpStatus, response.getStatusCode());
        assertEquals(code, response.getBody().getErrorCode());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @MethodSource("notificationErrorCodes_turkish")
    @ParameterizedTest(name = "TR - {0} should return \"{2}\"")
    void handleBaseException_shouldReturnTurkishMessage(String code, HttpStatus httpStatus, String expectedMessage) {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        BaseException ex = new BaseException(createErrorCode(code, httpStatus));

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals(httpStatus, response.getStatusCode());
        assertEquals(code, response.getBody().getErrorCode());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldReturnFormattedMessage_withArgs() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        BaseException ex = new BaseException(
                createErrorCode("MESSAGE_EXCEED_MAX_LENGTH", HttpStatus.BAD_REQUEST),
                255
        );

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Message cannot exceed 255 characters", response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldReturnFormattedTurkishMessage_withArgs() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        BaseException ex = new BaseException(
                createErrorCode("MESSAGE_EXCEED_MAX_LENGTH", HttpStatus.BAD_REQUEST),
                255
        );

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals("Mesaj 255 karakteri aşamaz", response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldReturnFormattedMessage_withMultipleArgs() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        BaseException ex = new BaseException(
                createErrorCode("INVALID_GSM", HttpStatus.BAD_REQUEST),
                10, 12
        );

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals("GSM number must be between 10 and 12 characters", response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldReturnFormattedTurkishMessage_withMultipleArgs() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        BaseException ex = new BaseException(
                createErrorCode("INVALID_GSM", HttpStatus.BAD_REQUEST),
                10, 12
        );

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals("GSM numarası 10 ile 12 karakter arasında olmalıdır", response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldFallbackToExplicitMessage_whenKeyNotInProperties() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        BaseException ex = new BaseException(
                createErrorCode("SOME_UNKNOWN_CODE", HttpStatus.INTERNAL_SERVER_ERROR),
                "Custom explicit message"
        );

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Custom explicit message", response.getBody().getMessage());
    }

    @Test
    void handleBaseException_shouldReturnCode_whenKeyNotInPropertiesAndNoExplicitMessage() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        BaseException ex = new BaseException(createErrorCode("SOME_UNKNOWN_CODE", HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertNotNull(response.getBody());
        assertEquals("SOME_UNKNOWN_CODE", response.getBody().getMessage());
    }

    @Test
    void handleException_shouldReturnLocalizedEnglishMessage() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("UNEXPECTED_ERROR", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleException_shouldReturnLocalizedTurkishMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("UNEXPECTED_ERROR", response.getBody().getErrorCode());
        assertEquals("Beklenmeyen bir hata oluştu", response.getBody().getMessage());
    }

    private ErrorCode createErrorCode(String code, HttpStatus httpStatus) {
        return new ErrorCode() {
            @Override
            public HttpStatus getHttpStatus() {
                return httpStatus;
            }

            @Override
            public String getCode() {
                return code;
            }
        };
    }
}
