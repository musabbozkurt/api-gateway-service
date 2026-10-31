package com.mb.notificationservice.util;

import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CryptoHelperTest {

    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static String referenceSha256Hex() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest("hello".getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String referenceHmacSha256Hex(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void toSha256Hex_ShouldReturnKnownDigest_WhenInputIsEmpty() {
        assertEquals(EMPTY_SHA256, CryptoHelper.toSha256Hex(""));
    }

    @Test
    void toSha256Hex_ShouldReturnKnownDigest_WhenInputIsHello() throws Exception {
        assertEquals(referenceSha256Hex(), CryptoHelper.toSha256Hex("hello"));
    }

    @Test
    void toSha256Hex_ShouldBeDeterministic_WhenCalledTwiceWithSameInput() {
        String input = "notification-body";

        assertEquals(CryptoHelper.toSha256Hex(input), CryptoHelper.toSha256Hex(input));
    }

    @Test
    void toSha256Hex_ShouldProduceDifferentDigests_WhenInputsDiffer() {
        assertNotEquals(CryptoHelper.toSha256Hex("a"), CryptoHelper.toSha256Hex("b"));
    }

    @Test
    void toSha256Hex_ShouldThrowBaseExceptionWithAlgorithmError_WhenSha256IsUnavailable() {
        try (MockedStatic<MessageDigest> messageDigest = mockStatic(MessageDigest.class)) {
            messageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new NoSuchAlgorithmException("SHA-256"));

            BaseException exception = assertThrows(BaseException.class, () -> CryptoHelper.toSha256Hex("data"));

            assertEquals(NotificationErrorCode.ALGORITHM_ERROR, exception.getErrorCode());
        }
    }

    @Test
    void toHashMac256_ShouldMatchJcaReference_WhenKeyAndDataAreProvided() throws Exception {
        String key = "testSecretKey";
        String data = "dateSignhttp://test.url" + referenceSha256Hex();

        assertEquals(referenceHmacSha256Hex(key, data), CryptoHelper.toHashMac256(key, data));
    }

    @Test
    void toHashMac256_ShouldChange_WhenKeyChanges() {
        String data = "payload";

        assertNotEquals(CryptoHelper.toHashMac256("key-a", data), CryptoHelper.toHashMac256("key-b", data));
    }

    @Test
    void toHashMac256_ShouldChange_WhenDataChanges() {
        String key = "secret";

        assertNotEquals(CryptoHelper.toHashMac256(key, "payload-a"), CryptoHelper.toHashMac256(key, "payload-b"));
    }

    @Test
    void toHashMac256_ShouldThrowBaseExceptionWithAlgorithmError_WhenHmacSha256IsUnavailable() {
        try (MockedStatic<Mac> mac = mockStatic(Mac.class)) {
            mac.when(() -> Mac.getInstance("HmacSHA256")).thenThrow(new NoSuchAlgorithmException("HmacSHA256"));

            BaseException exception = assertThrows(BaseException.class, () -> CryptoHelper.toHashMac256("key", "data"));

            assertEquals(NotificationErrorCode.ALGORITHM_ERROR, exception.getErrorCode());
        }
    }
}
