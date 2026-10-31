package com.mb.modulith.commerce.util;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertUtilsTest {

    private static final SharedErrorCode CODE = SharedErrorCode.NOT_FOUND;

    @Test
    void assertContains_ShouldPass_WhenListContainsElement() {
        // Arrange
        List<String> list = List.of("a", "b");

        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertContains(list, "a", CODE));
    }

    @Test
    void assertContains_ShouldThrow_WhenListIsEmpty() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertContains(Collections.emptyList(), "a", CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertContains_ShouldThrow_WhenElementMissing() {
        // Arrange
        List<String> list = List.of("a");

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertContains(list, "b", CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertNotContains_ShouldPass_WhenListDoesNotContainElement() {
        // Arrange
        List<String> list = List.of("a");

        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertNotContains(list, "b", CODE));
    }

    @Test
    void assertNotContains_ShouldPass_WhenListIsEmpty() {
        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertNotContains(Collections.emptyList(), "a", CODE));
    }

    @Test
    void assertNotContains_ShouldThrow_WhenElementPresent() {
        // Arrange
        List<String> list = List.of("a");

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertNotContains(list, "a", CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertNull_ShouldPass_WhenObjectIsNull() {
        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertNull(null, CODE));
    }

    @Test
    void assertNull_ShouldThrow_WhenObjectIsNotNull() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertNull("value", CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertNotNull_ShouldPass_WhenObjectIsPresent() {
        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertNotNull("value", CODE));
    }

    @Test
    void assertNotNull_ShouldThrow_WhenObjectIsNull() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertNotNull(null, CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertNotNullWithFormat_ShouldPass_WhenObjectIsPresent() {
        // Arrange
        SharedErrorCode clientMissing = SharedErrorCode.CLIENT_NOT_FOUND;

        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertNotNull("client-id", clientMissing));
    }

    @Test
    void assertNotNullWithFormat_ShouldThrow_WhenObjectIsNull() {
        // Arrange
        SharedErrorCode clientMissing = SharedErrorCode.CLIENT_NOT_FOUND;

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertNotNull(null, clientMissing));

        // Assertions
        assertEquals(clientMissing, ex.getErrorCode());
        assertEquals("Client not found", ex.getMessage());
    }

    @Test
    void assertTrue_ShouldPass_WhenExpressionIsTrue() {
        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertTrue(true, CODE));
    }

    @Test
    void assertTrue_ShouldThrow_WhenExpressionIsFalse() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertTrue(false, CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertTrueWithMessage_ShouldThrowWithMessage_WhenExpressionIsFalse() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertTrue(false, CODE, "custom message"));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
        assertEquals("custom message", ex.getMessage());
    }

    @Test
    void assertTrueWithFormat_ShouldThrow_WhenExpressionIsFalse() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertTrue(false, CODE, "id"));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertFalse_ShouldPass_WhenExpressionIsFalse() {
        // Act
        // Assertions
        assertDoesNotThrow(() -> AssertUtils.assertFalse(false, CODE));
    }

    @Test
    void assertFalse_ShouldThrow_WhenExpressionIsTrue() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertFalse(true, CODE));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }

    @Test
    void assertFalseWithMessage_ShouldThrowWithMessage_WhenExpressionIsTrue() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertFalse(true, CODE, "invalid state"));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
        assertEquals("invalid state", ex.getMessage());
    }

    @Test
    void assertFalseWithFormat_ShouldThrow_WhenExpressionIsTrue() {
        // Act
        BaseException ex = assertThrows(BaseException.class, () -> AssertUtils.assertFalse(true, CODE, "id"));

        // Assertions
        assertEquals(CODE, ex.getErrorCode());
    }
}