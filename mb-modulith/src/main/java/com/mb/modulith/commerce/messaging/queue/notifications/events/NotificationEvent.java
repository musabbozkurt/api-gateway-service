package com.mb.modulith.commerce.messaging.queue.notifications.events;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationEvent {

    private UUID eventId;
    @NotNull
    private TransferType transferType;
    private String destination;
    private List<String> cc;
    private String userId;
    private String content;
    @Builder.Default
    private List<Param> params = new ArrayList<>();
    private NotificationType notificationType;
    private String language;
    private HttpMethod httpMethod;
    @Builder.Default
    private List<Param> headers = new ArrayList<>();
    private Attachment attachment;

    public String toString() {
        return "NotificationEvent [destination=" + destination + ", eventId=" + eventId
                + ", destination=" + destination
                + ", language=" + language
                + ", notificationType=" + notificationType
                + ", transferType=" + transferType + "]";
    }


}
