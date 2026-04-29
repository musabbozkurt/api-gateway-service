package com.mb.notificationservice.queue.consumer;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.data.repository.NotificationRepository;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.mapper.NotificationMapper;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.NotificationTemplateResolver;
import com.mb.notificationservice.service.impl.NotificationStrategyFactory;
import com.mb.notificationservice.util.ServiceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationStrategyFactory strategyFactory;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationTemplateResolver notificationTemplateResolver;
    private final PlatformTransactionManager transactionManager;

    @KafkaListener(groupId = ServiceConstants.NOTIFICATION_GROUP, topics = ServiceConstants.NOTIFICATION_TOPIC)
    public void listen(NotificationEventDto eventDto) {
        log.info("Notification event consumed. Id: {}, Channel: {}", eventDto.getId(), eventDto.getChannel());

        Notification entity = notificationMapper.convert(eventDto);

        try {
            NotificationStrategy strategy = strategyFactory.getStrategy(eventDto.getChannel());
            NotificationRequest request = notificationMapper.toRequest(eventDto);
            notificationTemplateResolver.resolve(request);

            NotificationResponse response = strategy.send(request);

            if (response.isSuccess()) {
                entity.setStatus(NotificationStatus.SENT);
                log.info("Notification sent successfully. Id: {}, Channel: {}", eventDto.getId(), eventDto.getChannel());
            } else {
                entity.setStatus(NotificationStatus.FAILED);
                entity.setErrorMessage(response.getMessage());
                log.error("Notification failed. Id: {}, Channel: {}, Error: {}", eventDto.getId(), eventDto.getChannel(), response.getMessage());
            }

            notificationRepository.save(entity);
        } catch (Exception e) {
            log.error("Exception occurred while processing notification. Id: {}, Channel: {}, Exception: {}", eventDto.getId(), eventDto.getChannel(), ExceptionUtils.getStackTrace(e));
            entity.setStatus(NotificationStatus.FAILED);
            entity.setErrorMessage(e.getMessage());
            entity.setRetryCount(entity.getRetryCount() + 1);
            saveInNewTransaction(entity);
            throw e;
        }
    }

    private void saveInNewTransaction(Notification entity) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(_ -> notificationRepository.save(entity));
    }
}
