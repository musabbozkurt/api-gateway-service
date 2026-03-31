package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.service.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationStrategyFactoryTest {

    private NotificationStrategyFactory factory;

    @BeforeEach
    void setUp() {
        NotificationStrategy emailStrategy = mock(NotificationStrategy.class);
        when(emailStrategy.getChannel()).thenReturn(NotificationChannel.EMAIL);

        NotificationStrategy smsStrategy = mock(NotificationStrategy.class);
        when(smsStrategy.getChannel()).thenReturn(NotificationChannel.SMS);

        factory = new NotificationStrategyFactory(List.of(emailStrategy, smsStrategy));
        factory.init();
    }

    @Test
    void getStrategy_ShouldReturnStrategy_WhenChannelIsRegistered() {
        NotificationStrategy result = factory.getStrategy(NotificationChannel.EMAIL);

        assertNotNull(result);
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
    }

    @Test
    void getStrategy_ShouldReturnCorrectStrategy_WhenMultipleChannelsRegistered() {
        NotificationStrategy emailStrategy = factory.getStrategy(NotificationChannel.EMAIL);
        NotificationStrategy smsStrategy = factory.getStrategy(NotificationChannel.SMS);

        assertEquals(NotificationChannel.EMAIL, emailStrategy.getChannel());
        assertEquals(NotificationChannel.SMS, smsStrategy.getChannel());
    }

    @Test
    void getStrategy_ShouldThrowException_WhenChannelIsNotRegistered() {
        BaseException exception = assertThrows(BaseException.class, () -> factory.getStrategy(NotificationChannel.PUSH));

        assertEquals(NotificationErrorCode.UNSUPPORTED_NOTIFICATION_CHANNEL, exception.getErrorCode());
    }
}
