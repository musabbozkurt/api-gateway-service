package com.mb.modulith.commerce.messaging.queue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class holds queue names which will link consumer, producer and queue listener.
 * This is kept as a separate class since in the future, we can make use of this in
 * other micro services which can listen to queue which are configured by this micro service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueChannels {

    public static final String NOTIFICATION_PRODUCER = "notificationProducer-out-0";

}
