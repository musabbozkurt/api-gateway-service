package com.mb.swagger2.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

@Component
public class EntityIdUtils {

    private static StandardPBEStringEncryptor encryptionSupport;

    public EntityIdUtils() {
    }

    @Autowired
    public void setEncryptionSupport(StandardPBEStringEncryptor encryptionSupport) {
        EntityIdUtils.encryptionSupport = encryptionSupport;
    }

    public static Long decrypt(String value) {
        return Long.valueOf(encryptionSupport.decrypt(new String(Base64Utils.decodeFromUrlSafeString(value))));
    }

    public static String encrypt(Object object) {
        return Base64Utils.encodeToUrlSafeString(encryptionSupport.encrypt(object.toString()).getBytes());
    }
}