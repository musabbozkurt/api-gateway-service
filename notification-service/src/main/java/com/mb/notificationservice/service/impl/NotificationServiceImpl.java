package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.data.repository.NotificationRepository;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.mapper.NotificationMapper;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.queue.producer.NotificationEventProducer;
import com.mb.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationStrategyFactory strategyFactory;
    private final NotificationEventProducer notificationEventProducer;
    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse sendAsync(NotificationRequest request) {
        log.info("Queuing notification via channel: {}", request.getChannel());

        NotificationEventDto eventDto = notificationMapper.convert(request);
        notificationEventProducer.produce(eventDto);

        return NotificationResponse.builder()
                .id(eventDto.getId().toString())
                .channel(request.getChannel())
                .success(true)
                .message("Notification queued successfully")
                .build();
    }

    @Override
    public List<NotificationResponse> sendAsyncMultiple(List<NotificationRequest> requests) {
        log.info("Queuing {} notifications", requests.size());

        return requests.stream()
                .map(this::sendAsync)
                .toList();
    }

    @Override
    public NotificationResponse sendSync(NotificationRequest request) {
        log.info("Sending notification synchronously via channel: {}", request.getChannel());

        return strategyFactory.getStrategy(request.getChannel()).send(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationSummaryResponse> getNotifications(Pageable pageable, NotificationChannel channel) {
        Long userId = ContextHolder.getContext().userId();
        Page<Notification> page = Objects.nonNull(channel)
                ? notificationRepository.findByUserIdAndChannel(userId, channel, pageable)
                : notificationRepository.findByUserId(userId, pageable);
        return page.map(notificationMapper::toNotificationSummaryResponse);
    }

    @Override
    @Transactional
    public NotificationDetailResponse getNotificationDetailById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return notificationMapper.toNotificationDetailResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndIsReadFalse(ContextHolder.getContext().userId());
    }
}
