package com.mb.modulith.commerce.util;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertUtils {

    public static <T> void assertContains(List<T> list, T element, SharedErrorCode sharedErrorCode) {
        if (CollectionUtils.isEmpty(list) || !list.contains(element)) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static <T> void assertNotContains(List<T> list, T element, SharedErrorCode sharedErrorCode) {
        if (!CollectionUtils.isEmpty(list) && list.contains(element)) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static void assertNull(Object object, SharedErrorCode sharedErrorCode) {
        if (object != null) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static void assertNotNull(Object object, SharedErrorCode sharedErrorCode) {
        if (object == null) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static void assertTrue(boolean expression, SharedErrorCode sharedErrorCode) {
        if (!expression) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static void assertTrue(boolean expression, SharedErrorCode sharedErrorCode, String message) {
        if (!expression) {
            throw new BaseException(sharedErrorCode, message);
        }
    }

    public static void assertFalse(boolean expression, SharedErrorCode sharedErrorCode) {
        if (expression) {
            throw new BaseException(sharedErrorCode);
        }
    }

    public static void assertFalse(boolean expression, SharedErrorCode sharedErrorCode, String message) {
        if (expression) {
            throw new BaseException(sharedErrorCode, message);
        }
    }
}
