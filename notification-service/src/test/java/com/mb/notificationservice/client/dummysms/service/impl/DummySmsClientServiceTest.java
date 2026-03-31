package com.mb.notificationservice.client.dummysms.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class DummySmsClientServiceTest {

    @InjectMocks
    private DummySmsClientServiceImpl dummySmsClientService;

    @Test
    void sendSms_ShouldSendSuccessfully() {
        assertDoesNotThrow(() -> dummySmsClientService.sendSms("5554443322", "test message"));
    }

    @Test
    void sendSms_ShouldNotThrow_WhenPhoneNumberIsNull() {
        assertDoesNotThrow(() -> dummySmsClientService.sendSms(null, "test message"));
    }

    @Test
    void sendSms_ShouldNotThrow_WhenMessageIsNull() {
        assertDoesNotThrow(() -> dummySmsClientService.sendSms("5554443322", null));
    }
}
