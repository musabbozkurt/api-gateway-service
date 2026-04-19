# Notification Service

## Table of Contents

- [Project Description](#project-description)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [SSE Integration](#sse-integration)
- [Kafka Integration](#kafka-integration)

## Project Description

Notification Service is a Spring Boot-based enterprise-grade multi-channel notification platform. It enables other
services in a microservice architecture to send email, SMS, and real-time push notifications to users.

**Core Function:** Processes events asynchronously via Kafka or synchronously via REST API; delivers through email
(SMTP), SMS (DummySms), and push (Firebase FCM + SSE) channels; persists all notifications in PostgreSQL.

## Key Features

**Multi-Channel Notification**

- **EMAIL** — SMTP + Thymeleaf template support (HTML email)
- **SMS** — DummySms OTP integration
- **PUSH** — Firebase Cloud Messaging (FCM) + Server-Sent Events (SSE) for real-time web/mobile push

**Multi-Application Firebase Push**

- A single push notification can be sent to multiple Firebase applications simultaneously
- Application-scoped device token management (`DeviceToken` entity, `userId + application` unique)
- Bulk token query retrieves tokens for all applications in a single DB call
- Firebase service account files can be configured via `classpath:`, `file:`, or `https:`

**Real-time Communication (SSE)**

- One-way event stream from server to client over a single HTTP connection
- User + application scoped (`userId:application`) targeting
- Broadcast support (to all connected clients)
- `ConcurrentHashMap`-based thread-safe emitter registry

**Flexible Dispatch**

- **SYSTEM**: Broadcast to all connected clients
- **USER**: Push specific to a user (`userId`)
- **USER + APPLICATION**: Push specific to a user's specific applications

**Event-Driven Architecture**

- Asynchronous message processing with Kafka producer/consumer (`notification-topic`)
- Automatic retry with configurable backoff
- DLT (Dead Letter Topic) handler for tracking failed notifications
- Loosely-coupled integration from external services

**Template Management**

- `NotificationTemplate` entity: channel + code based template management (`code + channel` unique constraint)
- Dynamic HTML email templates with Thymeleaf
- REST API for template CRUD operations

**Data Persistence**

- Full notification history in PostgreSQL (`notification_schema`)
- Delivery status tracking: `PENDING → SENT / FAILED`
- Retry count and error message recording
- `NotificationStatus`, `NotificationLevel`, `NotificationChannel` enum support

## Architecture

### System Overview

```
┌─────────────────┐    REST API    ┌──────────────────────────┐
│  External       │───────────────▶│  NotificationController  │
│  Services       │                │                          │
└─────────────────┘                └──────────┬───────────────┘
                                              │ Kafka Produce
                                              ▼
┌─────────────────┐                ┌──────────────────────┐
│  External       │  Kafka Event   │  NotificationEvent   │
│  Services       │───────────────▶│     Consumer         │
│  (Kafka)        │                └──────────┬───────────┘
└─────────────────┘                           │
                                   ┌──────────┼──────────┐
                                   ▼          ▼          ▼
                             ┌──────────┐ ┌────────┐ ┌──────────┐
                             │  Email   │ │  SMS   │ │   Push   │
                             │ Strategy │ │Strategy│ │ Strategy │
                             └──────────┘ └────────┘ └──┬───┬───┘
                                                        │   │
                                              ┌─────────┘   └─────────┐
                                              ▼                       ▼
                                   ┌──────────────────┐    ┌──────────────────┐
                                   │  SseEmitter      │    │  Firebase FCM    │
                                   │  Registry        │    │  (per-app)       │
                                   └─────────┬────────┘    └──────────────────┘
                                             │ SSE Stream
                                   ┌─────────▼────────┐
                                   │  Web / Mobile    │
                                   │  Clients         │
                                   └──────────────────┘

                             ┌──────────────────────────────────────┐
                             │    PostgreSQL (notification_schema)   │
                             │  Notification + Template + Device    │
                             └──────────────────────────────────────┘
```

### Notification Channels

| Channel | Strategy               | Dispatch Logic                                  |
|---------|------------------------|-------------------------------------------------|
| EMAIL   | `EmailServiceImpl`     | SMTP via JavaMailSender + Thymeleaf             |
| SMS     | `SmsServiceImpl`       | DummySms REST API                               |
| PUSH    | `PushNotificationImpl` | SSE + Firebase FCM (multi-application per-user) |

### SSE Dispatch Rules

| Condition                         | Action                                        |
|-----------------------------------|-----------------------------------------------|
| `userId` + `applications` are set | `sendToKey(userId:app, event)` for each app   |
| `userId` is set, no applications  | `sendToUser(userId, event)` → all user's apps |
| Neither set                       | `broadcast(event)` → all clients              |

### Firebase Configuration

```yaml
firebase:
  apps:
    my-application: classpath:firebase/my-application-service-account.json
    another-application: file:/etc/config/another-service-account.json
```

Supported resource types:

- **Classpath**: `classpath:firebase/service-account.json`
- **File system**: `file:/etc/config/firebase-service-account.json`
- **URL**: `https://example.com/firebase/service-account.json`

### Data Model

```
notification_schema.notification
├── id                  (Long, sequence-generated)
├── channel             (EMAIL / SMS / PUSH)
├── type                (SYSTEM / USER / GROUP)
├── level               (INFO / SUCCESS / WARNING / ERROR / CRITICAL)
├── userId              (target user)
├── applications        (push target applications, comma-separated)
├── recipients          (email or phone, comma-separated)
├── cc                  (comma-separated)
├── bcc                 (comma-separated)
├── title
├── body
├── subject
├── templateCode
├── templateParameters  (JSON)
├── data                (JSON)
├── status              (PENDING / SENT / FAILED)
├── errorMessage
├── retryCount
├── isRead
└── readAt

notification_schema.notification_template
├── id                  (Long, sequence-generated)
├── code        ─┐ composite unique
├── channel     ─┘
├── name
├── subject
├── body                (HTML/Thymeleaf)
├── description
└── active

notification_schema.device_token
├── id                  (Long, sequence-generated)
├── userId      ─┐ composite unique
├── application ─┘
├── token               (FCM device token)
├── platform            (ANDROID / IOS / WEB)
└── active
```

## Getting Started

### Prerequisites

- Java 25+
- PostgreSQL
- Apache Kafka
- Maven (wrapper included)
- Docker (optional, for infrastructure services)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/musabbozkurt/api-gateway-service.git
   cd api-gateway-service/notification-service
   ```

2. Maven build:
   ```bash
   ./mvnw clean install
   ```

### Running the Application

```bash
./mvnw spring-boot:run
```

**Service URLs:**

- Application: `http://localhost:8085`
- Swagger UI: `http://localhost:8085/swagger-ui.html`
- Eureka: `http://localhost:8761/`

## API Endpoints

### Notification Send

```http
# Send asynchronously (queues to Kafka)
POST /api/v1/notifications/send
Content-Type: application/json

{
  "channel": "EMAIL",
  "level": "INFO",
  "userId": 12345,
  "recipients": ["user@example.com"],
  "subject": "Order Confirmed",
  "templateCode": "ORDER_CONFIRMATION",
  "templateParameters": {
    "orderNumber": "12345",
    "customerName": "John Doe"
  }
}
```

```http
# Push notification (to multiple applications)
POST /api/v1/notifications/send
Content-Type: application/json

{
  "channel": "PUSH",
  "level": "INFO",
  "applications": ["my-application", "another-application"],
  "userId": 12345,
  "title": "New Order",
  "body": "Your order has been prepared",
  "data": {
    "orderId": "12345",
    "action": "OPEN_ORDER"
  }
}
```

```http
# Batch async send
POST /api/v1/notifications/send/batch
Content-Type: application/json

[
  { "channel": "SMS", "level": "INFO", "recipients": ["905551234567"], "body": "Your code: 123456" },
  { "channel": "EMAIL", "level": "INFO", "recipients": ["user@example.com"], "subject": "Welcome" },
  { "channel": "PUSH", "level": "INFO", "applications": ["my-application"], "userId": 12345, "title": "Notification", "body": "Hello" }
]
```

```http
# Send synchronously (strategy called directly, Kafka bypassed)
POST /api/v1/notifications/send/sync
Content-Type: application/json

{
  "channel": "PUSH",
  "level": "INFO",
  "applications": ["my-application"],
  "userId": 12345,
  "title": "New Notification",
  "body": "Your order has been prepared"
}
```

```http
# SSE stream connection (application-scoped)
GET /api/v1/notifications/stream/user/{userId}/application/{application}
Accept: text/event-stream
```

### Device Token Registration

```http
# Register FCM device token
POST /api/v1/notifications/device-tokens
Content-Type: application/json

{
  "token": "fcm-device-token-abc123",
  "platform": "ANDROID",
  "application": "my-application"
}
```

### Notification Query

```http
# List user's notifications (paginated, optional channel filter)
GET /api/v1/notifications?channel=PUSH&page=0&size=20

# Notification detail (your own notifications only)
GET /api/v1/notifications/{id}

# Unread notification count
GET /api/v1/notifications/unread-count
```

### Notification Templates

```http
# Create template
POST /api/v1/templates

# Update template
PUT /api/v1/templates/{id}

# Get by ID
GET /api/v1/templates/{id}

# Get by code and channel
GET /api/v1/templates/code/{code}/channel/{channel}

# List all (paginated)
GET /api/v1/templates?page=0&size=20

# Delete
DELETE /api/v1/templates/{id}
```

### Request Model — `NotificationRequest`

| Field                | Type                  | Required | Description                                       |
|----------------------|-----------------------|----------|---------------------------------------------------|
| `channel`            | `NotificationChannel` | Yes      | `EMAIL`, `SMS`, `PUSH`                            |
| `level`              | `NotificationLevel`   | Yes      | `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `CRITICAL` |
| `applications`       | `Set<String>`         | No       | Push target applications (for PUSH channel)       |
| `recipients`         | `Set<String>`         | No       | Multiple or single recipient (email / phone)      |
| `userId`             | `Long`                | No       | Target user (required for PUSH)                   |
| `subject`            | `String`              | No       | Email subject                                     |
| `title`              | `String`              | No       | Notification title                                |
| `body`               | `String`              | No       | Notification content                              |
| `templateCode`       | `String`              | No       | Template code (used for EMAIL channel)            |
| `templateParameters` | `Map<String, Object>` | No       | Thymeleaf template variables                      |
| `data`               | `Map<String, String>` | No       | Additional metadata                               |
| `cc`                 | `Set<String>`         | No       | Email CC                                          |
| `bcc`                | `Set<String>`         | No       | Email BCC                                         |

## SSE Integration

### Connection

```javascript
const userId = 12345;
const application = 'my-application';
const url = `http://localhost:8085/api/v1/notifications/stream/user/${userId}/application/${application}`;

const eventSource = new EventSource(url);

eventSource.onmessage = function (event) {
    const notification = JSON.parse(event.data);
    console.log('Push notification received:', notification);
};

eventSource.addEventListener('connected', function (event) {
    console.log('SSE connection established:', event.data);
});

eventSource.onerror = function (err) {
    console.error('SSE connection error:', err);
    eventSource.close();
};
```

### Push Event Format

```json
{
  "channel": "PUSH",
  "userId": 12345,
  "applications": [
    "my-application"
  ],
  "title": "Order Prepared",
  "body": "Your order #12345 is ready for delivery",
  "data": {
    "orderId": "12345",
    "actionUrl": "/orders/12345"
  }
}
```

### SSE vs WebSocket

| Feature         | SSE                       | WebSocket            |
|-----------------|---------------------------|----------------------|
| Direction       | One-way (server → client) | Bidirectional        |
| Protocol        | HTTP/1.1                  | WS                   |
| Browser support | Native EventSource API    | Native WebSocket API |
| Proxy/Firewall  | No issues (HTTP)          | May cause issues     |
| Use case        | Notification push         | Chat, gaming         |

## Kafka Integration

### Topics

| Topic                | Consumer Group       | Description          |
|----------------------|----------------------|----------------------|
| `notification-topic` | `notification-group` | Async delivery queue |

### Async Flow

1. `POST /send` request arrives → `NotificationRequest` → `NotificationEventDto` → produced to Kafka
2. `NotificationEventConsumer` consumes → `NotificationStrategy.send()` is called
3. Success → `Notification.status = SENT`; failure → retry → DLT → `FAILED`

### Event Format (`notification-topic`)

```json
{
  "channel": "PUSH",
  "level": "INFO",
  "applications": [
    "my-application",
    "another-application"
  ],
  "userId": 12345,
  "title": "Order Confirmed",
  "body": "Your order has been prepared",
  "data": {
    "orderId": "12345"
  },
  "createdBy": 1001
}
```
