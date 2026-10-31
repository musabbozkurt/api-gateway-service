package com.mb.modulith.delivery.scheduler;

import com.mb.modulith.delivery.data.repository.DeliveryRepository;
import com.mb.modulith.delivery.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class DelayedDeliveryNotifier {

    private static final Duration CUSTOMER_SUPPORT_THRESHOLD = Duration.ofMinutes(45);

    private final DeliveryRepository deliveryRepository;
    private final DeliveryCustomerSupportNotifier customerSupportNotifier;

    @Scheduled(fixedDelay = 30000)
    public void checkDelayedDeliveries() {
        deliveryRepository.findAll().forEach(delivery -> {
            Duration duration = TimeUtils.deliveryDuration(delivery.getStartTime(), delivery.getEndTime());
            if (duration.compareTo(CUSTOMER_SUPPORT_THRESHOLD) >= 0) {
                customerSupportNotifier.notifyCustomerSupport(delivery);
            }
        });
    }
}
