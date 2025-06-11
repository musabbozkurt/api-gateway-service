package com.mb.paymentservice.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardUtilsTest {

    @Test
    void isCardValid_ShouldReturnTrue_WhenCardNumberIsValid() {
        // Arrange
        long validCard = 4242424242426742L;

        // Act
        boolean result = CardUtils.isCardValid(validCard);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isCardValid_ShouldReturnFalse_WhenCardNumberIsInvalid() {
        // Arrange
        long invalidCard = 1111111111111111L;

        // Act
        boolean result = CardUtils.isCardValid(invalidCard);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isCardValid_ShouldReturnTrue_WhenCardNumberIsAnotherValid() {
        // Arrange
        long validCard = 4532015112830366L;

        // Act
        boolean result = CardUtils.isCardValid(validCard);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isCardValid_ShouldReturnFalse_WhenCardNumberIsShortInvalid() {
        // Arrange
        long invalidCard = 1234567812345678L;

        // Act
        boolean result = CardUtils.isCardValid(invalidCard);

        // Assertions
        assertFalse(result);
    }
}
