package com.mb.swaggerapplication.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EntityIdUtilsIntegrationTest {

    @Test
    void testDecrypt() {
        // Encrypt a value using the encryption support
        String encryptedValue = EntityIdUtils.encrypt("123");

        // Decrypt the encrypted value using the entityIdUtils
        Long decryptedValue = EntityIdUtils.decrypt(encryptedValue);

        assertThat(decryptedValue).isEqualTo(123L);
    }

    @Test
    void testEncrypt() {
        // Encrypt a value using entityIdUtils
        String encryptedValue = EntityIdUtils.encrypt("456");

        // Decrypt the encrypted value using the encryption support
        long decryptedValue = EntityIdUtils.decrypt(encryptedValue);

        assertThat(decryptedValue).isEqualTo(456);
    }
}
