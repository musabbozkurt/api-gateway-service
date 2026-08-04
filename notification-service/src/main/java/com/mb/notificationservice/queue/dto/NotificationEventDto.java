package com.mb.notificationservice.queue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Data transfer object representing a notification event to be processed and dispatched.
 *
 * <p><b>PUSH notification example:</b></p>
 * <pre>{@code
 * {
 *   "channel": "PUSH",
 *   "level": "INFO",
 *   "applications": ["my-application", "my-second-application"],
 *   "userId": 12345,
 *   "title": "New Order",
 *   "body": "You have a new order to review",
 *   "data": {
 *     "orderId": "1234",
 *     "action": "OPEN_ORDER"
 *   },
 *   "createdBy": 1001
 * }
 * }</pre>
 *
 * <p><b>EMAIL notification example:</b></p>
 * <pre>{@code
 * {
 *   "channel": "EMAIL",
 *   "level": "INFO",
 *   "userId": 12345,
 *   "subject": "Order Confirmation",
 *   "body": "<p>Your order #1234 has been confirmed.</p>",
 *   "recipients": ["user@example.com"],
 *   "cc": ["manager@example.com"],
 *   "bcc": ["archive@example.com"],
 *   "attachments": [
 *     {
 *       "filename": "invoice.pdf",
 *       "contentType": "application/pdf",
 *       "contentBase64": "JVBERi0xLjQK..."
 *     }
 *   ],
 *   "createdBy": 1001
 * }
 * }</pre>
 *
 * <p><b>EMAIL with the template example:</b></p>
 * <pre>{@code
 * {
 *   "channel": "EMAIL",
 *   "level": "INFO",
 *   "userId": 12345,
 *   "recipients": ["user@example.com"],
 *   "templateCode": "ORDER_CONFIRMATION",
 *   "templateParameters": {
 *     "orderNumber": "1234",
 *     "customerName": "John Doe"
 *   },
 *   "createdBy": 1001
 * }
 * }</pre>
 *
 * <p><b>SMS notification example:</b></p>
 * <pre>{@code
 * {
 *   "channel": "SMS",
 *   "level": "INFO",
 *   "userId": 12345,
 *   "body": "Your verification code is 123456",
 *   "recipients": ["905321234567"],
 *   "createdBy": 1001
 * }
 * }</pre>
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotificationEventDto {

    /**
     * The unique identifier of the notification event.
     * <p>Example: {@code 12345}</p>
     */
    private UUID id;

    /**
     * The delivery channel for the notification.
     * <p>Example: {@code EMAIL}</p>
     */
    private NotificationChannel channel;

    /**
     * The severity level of the notification. Defaults to {@link NotificationLevel#INFO}.
     * <p>Example: {@code INFO}</p>
     */
    private NotificationLevel level = NotificationLevel.INFO;

    /**
     * The application identifiers that the notification belongs to.
     * <p>Example: {@code ["my-application", "my-second-application"]}</p>
     */
    private Set<String> applications = new HashSet<>();

    /**
     * The subject line of the notification (used primarily for email notifications).
     * <p>Example: {@code "Order Confirmation"}</p>
     */
    private String subject;

    /**
     * The body content of the notification message.
     * <p>Example: {@code "<p>Your order #1234 has been confirmed.</p>"}</p>
     */
    private String body;

    /**
     * The title of the notification (used primarily for push notifications).
     * <p>Example: {@code "New Order"}</p>
     */
    private String title;

    /**
     * The template code used to render the notification content.
     * <p>Example: {@code "ORDER_CONFIRMATION"}</p>
     */
    private String templateCode;

    /**
     * Dynamic parameters to be injected into the notification template.
     * <p>Example: {@code {"orderNumber": "1234", "customerName": "John Doe"}}</p>
     */
    private Map<String, Object> templateParameters = new HashMap<>();

    /**
     * Additional key-value data payload attached to the notification.
     * <p>Example: {@code {"orderId": "1234", "action": "OPEN_ORDER"}}</p>
     */
    private Map<String, String> data = new HashMap<>();

    /**
     * The ID of the target user who will receive the notification.
     * <p>Example: {@code 12345}</p>
     */
    private Long userId;

    /**
     * The set of recipient addresses (e.g., email addresses or phone numbers).
     * <p>Example: {@code ["user@example.com"]}</p>
     */
    private Set<String> recipients = new HashSet<>();

    /**
     * The set of CC (carbon copy) recipient addresses.
     * <p>Example: {@code ["manager@example.com"]}</p>
     */
    private Set<String> cc = new HashSet<>();

    /**
     * The set of BCC (blind carbon copy) recipient addresses.
     * <p>Example: {@code ["archive@example.com"]}</p>
     */
    private Set<String> bcc = new HashSet<>();

    /**
     * File attachments for email notifications (PDF, Excel, Word, etc.).
     * Content is Base64-encoded. Not persisted on the notification entity.
     */
    private List<AttachmentDto> attachments = new ArrayList<>();

    /**
     * The ID of the user who created/triggered the notification.
     * <p>Example: {@code 1001}</p>
     */
    private Long createdBy;

    public NotificationEventDto() {
        this.id = UUID.randomUUID();
    }
}
