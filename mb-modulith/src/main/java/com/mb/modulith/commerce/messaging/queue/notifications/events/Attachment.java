package com.mb.modulith.commerce.messaging.queue.notifications.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Attachment {
    private String fileName;
    private byte[] attachmentData;
}
