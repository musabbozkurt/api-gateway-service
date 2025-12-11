package com.mb.swaggerapplication.utils;

import com.mb.swaggerapplication.exception.CustomRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityIdUtilsIntegrationTest {

    private static Stream<Arguments> provideValuesForLongDecryption() {
        return Stream.of(
                Arguments.of("0", 0L),
                Arguments.of("1", 1L),
                Arguments.of("-1", -1L),
                Arguments.of("9223372036854775807", Long.MAX_VALUE),
                Arguments.of("-9223372036854775808", Long.MIN_VALUE)
        );
    }

    private static Stream<Arguments> provideValuesForStringDecryption() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("a"),
                Arguments.of("hello world"),
                Arguments.of("special!@#$%^&*()chars"),
                Arguments.of("unicode-тест-テスト")
        );
    }

    @Test
    void encrypt_ShouldReturnEncodedString_WhenValidDataProvided() {
        // Arrange
        String originalValue = "123";

        // Act
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Assertions
        assertThat(encryptedValue)
                .isNotNull()
                .isNotEmpty()
                .isNotEqualTo(originalValue);
    }

    @Test
    void decrypt_ToLong_ShouldReturnOriginalValue_WhenValidEncryptedDataProvided() {
        // Arrange
        String originalValue = "456";
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        Long decryptedValue = EntityIdUtils.decryptToLong(encryptedValue);

        // Assertions
        assertThat(decryptedValue).isEqualTo(456L);
    }

    @Test
    void encryptAndDecrypt_ToLong_ShouldReturnOriginalValue_WhenRoundTripPerformed() {
        // Arrange
        String originalValue = "789";

        // Act
        String encryptedValue = EntityIdUtils.encrypt(originalValue);
        Long decryptedValue = EntityIdUtils.decryptToLong(encryptedValue);

        // Assertions
        assertThat(decryptedValue).isEqualTo(789L);
    }

    @Test
    void decrypt_ToLong_ShouldThrowCustomRuntimeException_WhenInvalidEncryptedDataProvided() {
        // Arrange
        String invalidEncryptedData = "invalidData!!!";

        // Act
        // Assertions
        assertThatThrownBy(() -> EntityIdUtils.decryptToLong(invalidEncryptedData))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining("Error while");
    }

    @Test
    void encrypt_ShouldProduceDifferentOutputs_WhenCalledMultipleTimes() {
        // Arrange
        String originalValue = "100";

        // Act
        String encryptedValue1 = EntityIdUtils.encrypt(originalValue);
        String encryptedValue2 = EntityIdUtils.encrypt(originalValue);

        // Assertions
        assertThat(encryptedValue1).isNotNull();
        assertThat(encryptedValue2).isNotNull();
        assertThat(encryptedValue1).isNotEqualTo(encryptedValue2);
    }

    @Test
    void decrypt_ToString_ShouldReturnOriginalValue_WhenValidEncryptedDataProvided() {
        // Arrange
        String originalValue = "testString";
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        String decryptedValue = EntityIdUtils.decryptToString(encryptedValue);

        // Assertions
        assertThat(decryptedValue).isEqualTo(originalValue);
    }

    @Test
    void decrypt_ToInteger_ShouldReturnOriginalValue_WhenValidEncryptedDataProvided() {
        // Arrange
        String originalValue = "42";
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        Integer decryptedValue = EntityIdUtils.decrypt(encryptedValue, Integer.class);

        // Assertions
        assertThat(decryptedValue).isEqualTo(42);
    }

    @Test
    void decrypt_ToDouble_ShouldReturnOriginalValue_WhenValidEncryptedDataProvided() {
        // Arrange
        String originalValue = "3.14159";
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        Double decryptedValue = EntityIdUtils.decrypt(encryptedValue, Double.class);

        // Assertions
        assertThat(decryptedValue).isEqualTo(3.14159);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void decrypt_ToBoolean_ShouldReturnOriginalValue_WhenValidEncryptedDataProvided(String originalValue) {
        // Arrange
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        Boolean decryptedValue = EntityIdUtils.decrypt(encryptedValue, Boolean.class);

        // Assertions
        assertThat(decryptedValue).isEqualTo(Boolean.valueOf(originalValue));
    }

    @Test
    void decrypt_ShouldThrowCustomRuntimeException_WhenUnsupportedTypeProvided() {
        // Arrange
        String originalValue = "123";
        String encryptedValue = EntityIdUtils.encrypt(originalValue);

        // Act
        // Assertions
        assertThatThrownBy(() -> EntityIdUtils.decrypt(encryptedValue, Float.class))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining("Unsupported target type");
    }

    @ParameterizedTest
    @MethodSource("provideValuesForLongDecryption")
    void decrypt_ToLong_ShouldReturnCorrectValue_WhenVariousLongValuesProvided(String input, Long expected) {
        // Arrange
        String encryptedValue = EntityIdUtils.encrypt(input);

        // Act
        Long decryptedValue = EntityIdUtils.decryptToLong(encryptedValue);

        // Assertions
        assertThat(decryptedValue).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForStringDecryption")
    void decrypt_ToString_ShouldReturnCorrectValue_WhenVariousStringValuesProvided(String input) {
        // Arrange
        String encryptedValue = EntityIdUtils.encrypt(input);

        // Act
        String decryptedValue = EntityIdUtils.decrypt(encryptedValue, String.class);

        // Assertions
        assertThat(decryptedValue).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "!!!", "not-base64-@@@", ""})
    void decrypt_ShouldThrowCustomRuntimeException_WhenInvalidBase64Provided(String invalidData) {
        // Arrange
        // Act
        // Assertions
        assertThatThrownBy(() -> EntityIdUtils.decryptToString(invalidData))
                .isInstanceOf(CustomRuntimeException.class)
                .hasMessageContaining("Error while");
    }

    @Test
    void decrypt_ToLong_ShouldThrowCustomRuntimeException_WhenNonNumericValueEncrypted() {
        // Arrange
        String nonNumericValue = "notANumber";
        String encryptedValue = EntityIdUtils.encrypt(nonNumericValue);

        // Act
        // Assertions
        assertThatThrownBy(() -> EntityIdUtils.decryptToLong(encryptedValue))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void decrypt_ToInteger_ShouldThrowNumberFormatException_WhenValueExceedsIntegerRange() {
        // Arrange
        String largeValue = "9223372036854775807";
        String encryptedValue = EntityIdUtils.encrypt(largeValue);

        // Act
        // Assertions
        assertThatThrownBy(() -> EntityIdUtils.decrypt(encryptedValue, Integer.class))
                .isInstanceOf(NumberFormatException.class);
    }
}
