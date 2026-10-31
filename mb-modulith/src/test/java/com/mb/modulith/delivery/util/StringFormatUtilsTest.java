package com.mb.modulith.delivery.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringFormatUtilsTest {

    @Test
    void getStringFrom_ShouldFormatMessage_WhenPlaceholdersProvided() {
        // Arrange
        ResourceBundle bundle = ResourceBundle.getBundle(DeliveryConstants.MESSAGES_BASENAME, Locale.US);

        // Act
        String result = StringFormatUtils.getStringFrom(bundle, DeliveryConstants.APPLICATION_TEST_MESSAGE_KEY, "a", "b");

        // Assertions
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test
    void getStringFrom_ShouldReturnExpectedMessage_WhenLocaleIsUs() {
        // Arrange
        ResourceBundle bundle = ResourceBundle.getBundle(DeliveryConstants.MESSAGES_BASENAME, Locale.US);

        // Act
        String result = StringFormatUtils.getStringFrom(bundle, DeliveryConstants.APPLICATION_TEST_MESSAGE_KEY, "Jack", "Mike");

        // Assertions
        assertEquals("Default Hello There - Jack Mike", result);
    }

    @Test
    void getStringFrom_ShouldSupportTurkishLocaleBundle_WhenLocaleIsTr() {
        // Arrange
        ResourceBundle bundle = ResourceBundle.getBundle(DeliveryConstants.MESSAGES_BASENAME, Locale.forLanguageTag("tr"));

        // Act
        String result = StringFormatUtils.getStringFrom(bundle, DeliveryConstants.APPLICATION_TEST_MESSAGE_KEY, "ğĞüÜşŞöÖçÇ", "test");

        // Assertions
        assertEquals("Default Hello There - ğĞüÜşŞöÖçÇ test", result);
    }
}
