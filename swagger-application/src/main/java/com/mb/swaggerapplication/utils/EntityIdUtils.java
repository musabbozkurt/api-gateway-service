package com.mb.swaggerapplication.utils;

import com.mb.swaggerapplication.exception.CustomRuntimeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityIdUtils {

    private static final String SECRET_KEY = "YsrBlfW5zR5eaRQT";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 16;
    private static final int GCM_TAG_LENGTH = 128;

    public static String encrypt(String data) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            byte[] encryptedValue = cipher.doFinal(data.getBytes());
            byte[] encryptedWithIv = new byte[iv.length + encryptedValue.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedValue, 0, encryptedWithIv, iv.length, encryptedValue.length);
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            log.error("Error while encrypting: {}", ExceptionUtils.getStackTrace(e));
            throw new CustomRuntimeException("Error while encrypting: " + e.getMessage());
        }
    }

    public static Long decryptToLong(String encryptedData) {
        return decrypt(encryptedData, Long.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T decrypt(String encryptedData, Class<T> targetType) {
        String decryptedString = decryptToString(encryptedData);

        if (targetType == String.class) {
            return (T) decryptedString;
        } else if (targetType == Long.class) {
            return (T) Long.valueOf(decryptedString);
        } else if (targetType == Integer.class) {
            return (T) Integer.valueOf(decryptedString);
        } else if (targetType == Double.class) {
            return (T) Double.valueOf(decryptedString);
        } else if (targetType == Boolean.class) {
            return (T) Boolean.valueOf(decryptedString);
        } else {
            throw new CustomRuntimeException("Unsupported target type: " + targetType.getName());
        }
    }

    public static String decryptToString(String encryptedData) {
        try {
            Key key = generateKey();
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            if (encryptedWithIv.length < GCM_IV_LENGTH) {
                throw new CustomRuntimeException("Error while decrypting: Invalid encrypted data length");
            }
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[encryptedWithIv.length - iv.length];
            System.arraycopy(encryptedWithIv, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            byte[] decryptedValue = cipher.doFinal(encrypted);
            return new String(decryptedValue);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException |
                 IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            log.error("Error while decrypting: {}", ExceptionUtils.getStackTrace(e));
            throw new CustomRuntimeException("Error while decrypting: " + e.getMessage());
        }
    }

    private static Key generateKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
    }
}
