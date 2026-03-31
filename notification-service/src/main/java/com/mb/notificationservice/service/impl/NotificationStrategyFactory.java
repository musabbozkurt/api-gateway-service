package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.service.NotificationStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationStrategyFactory {

    private final List<NotificationStrategy> strategies;
    private Map<NotificationChannel, NotificationStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = new EnumMap<>(NotificationChannel.class);
        strategies.forEach(strategy -> strategyMap.put(strategy.getChannel(), strategy));
    }

    public NotificationStrategy getStrategy(NotificationChannel channel) {
        NotificationStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new BaseException(NotificationErrorCode.UNSUPPORTED_NOTIFICATION_CHANNEL);
        }
        return strategy;
    }
}
