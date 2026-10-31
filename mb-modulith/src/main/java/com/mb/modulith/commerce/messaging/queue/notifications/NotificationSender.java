package com.mb.modulith.commerce.messaging.queue.notifications;

import com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationType;
import com.mb.modulith.commerce.messaging.queue.notifications.events.Param;
import com.mb.modulith.commerce.messaging.queue.notifications.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.format.AmountFormatParams;
import org.javamoney.moneta.format.CurrencyStyle;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private static final String DEFAULT_LANGUAGE = "TR";
    private static final MonetaryAmountFormat MONEY_FORMAT = MonetaryFormats.getAmountFormat(
            AmountFormatQueryBuilder.of(Locale.of("tr", "TR"))
                    .set(CurrencyStyle.SYMBOL)
                    .set(AmountFormatParams.PATTERN, "¤###,###.##")
                    .build()
    );

    private final NotificationProducer notificationProducer;

    public void sendNotification(MonetaryAmount amount, UUID receiverTenantId, String merchantName, NotificationType notificationType) {
        ArrayList<Param> params = new ArrayList<>();
        params.add(new Param("amount", MONEY_FORMAT.format(amount)));
        params.add(new Param("merchantName", merchantName));
        log.info("Send notification : {}.", merchantName);

        notificationProducer.sendNotification(notificationType, params, DEFAULT_LANGUAGE, receiverTenantId);
    }

}

