package com.mb.modulith.commerce.messaging.queue.notifications.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Used to determine what transfer medium is to be used when transporting a notification.
 */
@Getter
@AllArgsConstructor
public enum TransferType {

    EMAIL(true, false),
    SMS(true, false),
    WEBHOOK(false, true),
    SLACK(false, false),
    NOTIFICATION(false, false);

    private final boolean isEmailOrSms;
    private final boolean isWebhook;
}
