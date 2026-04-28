package com.mb.notificationservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    void isHtml_ShouldReturnFalse_WhenContentIsNullOrEmpty(String content) {
        assertFalse(ContentUtils.isHtml(content));
    }

    @Test
    void isHtml_ShouldReturnFalse_WhenContentIsPlainText() {
        assertFalse(ContentUtils.isHtml("Hello {{name}}, your order is ready."));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<!DOCTYPE html><html><body>Hello</body></html>",
            "<html><body>Hello</body></html>",
            "<body>Hello</body>",
            "<table><tr><td>Data</td></tr></table>",
            "<div>Hello World</div>",
            "<p th:text=\"${name}\">Name</p>",
            "Hello [[${name}]]"
    })
    void isHtml_ShouldReturnTrue_WhenContentContainsHtmlOrThymeleafMarkers(String content) {
        assertTrue(ContentUtils.isHtml(content));
    }

    @Test
    void isHtml_ShouldReturnTrue_WhenContentHasLeadingWhitespace() {
        assertTrue(ContentUtils.isHtml("   <div>Hello</div>"));
    }

    @Test
    void isHtml_ShouldReturnFalse_WhenContentHasNonHtmlAngleBrackets() {
        assertFalse(ContentUtils.isHtml("5 < 10 and 10 > 5"));
    }
}
