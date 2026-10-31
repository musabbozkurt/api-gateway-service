package com.mb.modulith.commerce.messaging.queue.notifications.consumer;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.messaging.queue.ConsumerEventStrategy;
import com.mb.modulith.commerce.service.OrderService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.UUID;

import static com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationType.ORDER_CREATED;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationConsumerStrategy implements ConsumerEventStrategy {

    private final OrderService orderService;

    @Override
    public void execute(Message<?> event) {
        try {
            log.info("Received an order created event. NotificationConsumer - ORDER_CREATED: {} headers: {}.", event.getPayload(), event.getHeaders());
            orderService.updateOrder(UUID.randomUUID(), null, null);
        } catch (Exception _) {
            log.error("Exception occurred while executing NotificationConsumerConfig. NotificationConsumer - ORDER_CREATED: {} headers: {}.", event.getPayload(), event.getHeaders());
            throw new BaseException(SharedErrorCode.UNEXPECTED_ERROR);
        }
    }

    @Override
    public boolean canExecute(String eventType) {
        return ORDER_CREATED.name().equals(eventType);
    }
}
