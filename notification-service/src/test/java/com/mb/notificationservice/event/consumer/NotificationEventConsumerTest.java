package com.mb.notificationservice.event.consumer;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.data.repository.NotificationRepository;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.mapper.NotificationMapper;
import com.mb.notificationservice.queue.consumer.NotificationEventConsumer;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.impl.NotificationStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Mock
    private NotificationStrategyFactory strategyFactory;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationStrategy strategy;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @Test
    void listen_ShouldSaveWithSentStatus_WhenStrategySendSucceeds() {
        NotificationEventDto eventDto = createEventDto(NotificationChannel.EMAIL);
        Notification entity = new Notification();
        NotificationRequest request = new NotificationRequest();

        when(notificationMapper.convert(eventDto)).thenReturn(entity);
        when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(strategy);
        when(notificationMapper.toRequest(eventDto)).thenReturn(request);
        when(strategy.send(request)).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.listen(eventDto);

        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(NotificationStatus.SENT, notificationCaptor.getValue().getStatus());
        assertEquals(0, notificationCaptor.getValue().getRetryCount());
    }

    @Test
    void listen_ShouldSaveWithFailedStatus_WhenStrategyReturnsFalse() {
        NotificationEventDto eventDto = createEventDto(NotificationChannel.EMAIL);
        Notification entity = new Notification();
        NotificationRequest request = new NotificationRequest();

        when(notificationMapper.convert(eventDto)).thenReturn(entity);
        when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(strategy);
        when(notificationMapper.toRequest(eventDto)).thenReturn(request);
        when(strategy.send(request)).thenReturn(NotificationResponse.builder().success(false).message("delivery failed").build());

        consumer.listen(eventDto);

        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(NotificationStatus.FAILED, notificationCaptor.getValue().getStatus());
        assertEquals("delivery failed", notificationCaptor.getValue().getErrorMessage());
    }

    @Test
    void listen_ShouldSaveFailedWithRetryCountAndRethrow_WhenGetStrategyThrowsException() {
        NotificationEventDto eventDto = createEventDto(NotificationChannel.EMAIL);
        Notification entity = new Notification();

        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(notificationMapper.convert(eventDto)).thenReturn(entity);
        when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> consumer.listen(eventDto));
        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(NotificationStatus.FAILED, notificationCaptor.getValue().getStatus());
        assertEquals("boom", notificationCaptor.getValue().getErrorMessage());
        assertEquals(1, notificationCaptor.getValue().getRetryCount());
    }

    @ParameterizedTest
    @EnumSource(NotificationChannel.class)
    void listen_ShouldSaveWithSentStatus_WhenChannelIsValid(NotificationChannel channel) {
        NotificationEventDto eventDto = createEventDto(channel);
        Notification entity = new Notification();
        NotificationRequest request = new NotificationRequest();

        when(notificationMapper.convert(eventDto)).thenReturn(entity);
        when(strategyFactory.getStrategy(channel)).thenReturn(strategy);
        when(notificationMapper.toRequest(eventDto)).thenReturn(request);
        when(strategy.send(any())).thenReturn(NotificationResponse.builder().success(true).build());

        consumer.listen(eventDto);

        verify(strategyFactory).getStrategy(channel);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(NotificationStatus.SENT, notificationCaptor.getValue().getStatus());
    }

    private NotificationEventDto createEventDto(NotificationChannel channel) {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(channel);
        return dto;
    }
}
