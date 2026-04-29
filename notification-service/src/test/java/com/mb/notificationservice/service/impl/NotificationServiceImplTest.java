package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.data.repository.NotificationRepository;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.mapper.NotificationMapper;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.queue.producer.NotificationEventProducer;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.NotificationTemplateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final Long USER_ID = 12345L;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationStrategyFactory strategyFactory;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateResolver notificationTemplateResolver;

    @BeforeEach
    void setUp() {
        ContextHolder.setContext(ContextHolder.Context.builder().userId(USER_ID).build());
    }

    @Test
    void sendAsync_ShouldQueueAndReturnResponse_WhenRequestIsValid() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);

        NotificationEventDto eventDto = new NotificationEventDto();
        when(notificationMapper.convert(request)).thenReturn(eventDto);

        NotificationResponse response = notificationService.sendAsync(request);

        assertNotNull(response);
        assertEquals(eventDto.getId().toString(), response.getId());
        assertEquals(NotificationChannel.EMAIL, response.getChannel());
        assertTrue(response.isSuccess());
        verify(notificationEventProducer).produce(eventDto);
    }

    @Test
    void sendAsyncMultiple_ShouldQueueAll_WhenMultipleRequestsProvided() {
        NotificationRequest request1 = new NotificationRequest();
        request1.setChannel(NotificationChannel.EMAIL);

        NotificationRequest request2 = new NotificationRequest();
        request2.setChannel(NotificationChannel.SMS);

        when(notificationMapper.convert(any(NotificationRequest.class))).thenReturn(new NotificationEventDto());

        List<NotificationResponse> responses = notificationService.sendAsyncMultiple(List.of(request1, request2));

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(NotificationResponse::isSuccess));
    }

    @Test
    void sendSync_ShouldDelegateToStrategy_WhenRequestIsValid() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.PUSH);

        NotificationStrategy strategy = mock(NotificationStrategy.class);
        NotificationResponse expected = NotificationResponse.builder().channel(NotificationChannel.PUSH).success(true).build();

        when(strategyFactory.getStrategy(NotificationChannel.PUSH)).thenReturn(strategy);
        when(strategy.send(request)).thenReturn(expected);

        NotificationResponse response = notificationService.sendSync(request);

        assertEquals(expected, response);
        verify(notificationTemplateResolver).resolve(request);
        verify(strategyFactory).getStrategy(NotificationChannel.PUSH);
    }

    @Test
    void getNotifications_ShouldFilterByChannel_WhenChannelIsProvided() {
        Pageable pageable = PageRequest.of(0, 20);
        Pageable sortedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
        Notification entity = new Notification();
        Page<Notification> entityPage = new PageImpl<>(List.of(entity), sortedPageable, 1);

        NotificationSummaryResponse summary = NotificationSummaryResponse.builder().id(1L).channel(NotificationChannel.PUSH).build();

        when(notificationRepository.findByUserIdAndChannel(any(), any(), any(Pageable.class))).thenReturn(entityPage);
        when(notificationMapper.toNotificationSummaryResponse(entity)).thenReturn(summary);

        Page<NotificationSummaryResponse> result = notificationService.getNotifications(pageable, NotificationChannel.PUSH);

        assertEquals(1, result.getTotalElements());
        verify(notificationRepository).findByUserIdAndChannel(any(), any(), any(Pageable.class));
    }

    @Test
    void getNotifications_ShouldReturnAll_WhenChannelIsNull() {
        Pageable pageable = PageRequest.of(0, 20);
        Pageable sortedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
        Notification entity = new Notification();
        Page<Notification> entityPage = new PageImpl<>(List.of(entity), sortedPageable, 1);

        NotificationSummaryResponse summary = NotificationSummaryResponse.builder().id(1L).channel(NotificationChannel.PUSH).build();

        when(notificationRepository.findByUserId(any(), any(Pageable.class))).thenReturn(entityPage);
        when(notificationMapper.toNotificationSummaryResponse(entity)).thenReturn(summary);

        Page<NotificationSummaryResponse> result = notificationService.getNotifications(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(notificationRepository).findByUserId(any(), any(Pageable.class));
        verify(notificationRepository, never()).findByUserIdAndChannel(any(), any(), any());
    }

    @Test
    void getUnreadCount_ShouldReturnCount_WhenUserHasUnreadNotifications() {
        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(5L);

        long count = notificationService.getUnreadCount();

        assertEquals(5L, count);
    }

    @Test
    void getNotificationDetailById_ShouldMarkAsReadAndReturnDetail_WhenNotificationIsUnread() {
        Notification entity = new Notification();
        entity.setId(1L);
        entity.setRead(false);

        NotificationDetailResponse detail = NotificationDetailResponse.builder().id(1L).channel(NotificationChannel.EMAIL).level(NotificationLevel.INFO).status(NotificationStatus.SENT).build();

        when(notificationRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(entity));
        when(notificationRepository.save(entity)).thenReturn(entity);
        when(notificationMapper.toNotificationDetailResponse(entity)).thenReturn(detail);

        NotificationDetailResponse result = notificationService.getNotificationDetailById(1L);

        assertEquals(1L, result.getId());
        assertTrue(entity.isRead());
        assertNotNull(entity.getReadAt());
        verify(notificationRepository).save(entity);
    }

    @Test
    void getNotificationDetailById_ShouldNotUpdateReadStatus_WhenNotificationIsAlreadyRead() {
        Notification entity = new Notification();
        entity.setId(1L);
        entity.setRead(true);
        LocalDateTime previousReadAt = LocalDateTime.of(2026, 3, 1, 10, 0);
        entity.setReadAt(previousReadAt);

        NotificationDetailResponse detail = NotificationDetailResponse.builder().id(1L).build();

        when(notificationRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(entity));
        when(notificationMapper.toNotificationDetailResponse(entity)).thenReturn(detail);

        notificationService.getNotificationDetailById(1L);

        assertEquals(previousReadAt, entity.getReadAt());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getNotificationDetailById_ShouldThrowException_WhenNotificationNotFound() {
        when(notificationRepository.findByIdAndUserId(999L, USER_ID)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> notificationService.getNotificationDetailById(999L));

        assertEquals(NotificationErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
    }
}
