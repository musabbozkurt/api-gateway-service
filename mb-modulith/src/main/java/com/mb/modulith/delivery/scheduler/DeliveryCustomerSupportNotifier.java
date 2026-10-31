package com.mb.modulith.delivery.scheduler;

import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryCustomerSupportNotifier {

    @Async
    public void notifyCustomerSupport(Delivery delivery) {
        log.info("Customer support team is notified for delivery id {} (duration {} minutes)!", delivery.getId(), TimeUtils.deliveryDuration(delivery.getStartTime(), delivery.getEndTime()).toMinutes());
    }
}
