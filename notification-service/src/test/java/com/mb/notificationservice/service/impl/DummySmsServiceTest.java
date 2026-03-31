package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.client.dummysms.service.impl.DummySmsClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class DummySmsServiceTest {

    @InjectMocks
    private DummySmsClientServiceImpl dummySmsClientService;

    @Test
    void sendSms_ShouldComplete_WhenValidInput() {
        assertDoesNotThrow(() -> dummySmsClientService.sendSms("5554443322", "test message"));
    }

    @Test
    void sendSms_ShouldComplete_WhenEmptyMessage() {
        assertDoesNotThrow(() -> dummySmsClientService.sendSms("5554443322", ""));
    }

    @Test
    void sendSms_ShouldComplete_WhenLongMessage() {
        String longMessage = "A".repeat(500);
        assertDoesNotThrow(() -> dummySmsClientService.sendSms("5554443322", longMessage));
    }
}
