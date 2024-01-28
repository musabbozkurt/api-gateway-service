package com.mb.swaggerapplication.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;

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
        return Long.valueOf(encryptionSupport.decrypt(new String(Base64.getUrlDecoder().decode(value))));
    }

    public static String encrypt(Object object) {
        return Base64.getUrlEncoder().encodeToString(encryptionSupport.encrypt(object.toString()).getBytes());
    }
}