# Swagger Application

## Description

Swagger Application is a sample microservice that demonstrates **Spring Cloud Stream** event-driven messaging, **Spring
Cloud Config** integration, and **Spring Cloud Bus** for real-time config refresh. It communicates with Student Service
via RabbitMQ events.

## Key Features

- **Event-Driven Messaging** — RabbitMQ producer/consumer with Spring Cloud Stream
- **Dual Message Broker** — RabbitMQ for application events, Kafka for Spring Cloud Bus
- **Spring Cloud Config Client** — Fetches configuration from Config Server with fail-fast
- **Spring Cloud Bus** — Real-time config refresh via Kafka (`/actuator/busrefresh`)
- **Dead Letter Queue** — Automatic DLQ with TTL for failed messages
- **Micrometer Tracing** — Distributed tracing with trace/span ID propagation
- **Spring Cloud Eureka Client** — Service discovery registration
- **`@RefreshScope`** — Config properties auto-refresh without restart

## Tech Stack

- Java 25, Spring Boot 4.0
- Spring Cloud Stream + RabbitMQ + Kafka
- Spring Cloud Config Client + Bus
- H2 (in-memory database)
- SpringDoc OpenAPI
- Micrometer Tracing (Brave)

## Configuration

| Property      | Default                 | Description               |
|---------------|-------------------------|---------------------------|
| `server.port` | `8082`                  | Service port              |
| RabbitMQ      | `localhost:5673`        | Application event broker  |
| Kafka         | `localhost:9092`        | Spring Cloud Bus broker   |
| Config Server | `localhost:8888`        | Centralized config source |
| Eureka        | `localhost:8761/eureka` | Service registry          |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8082`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- H2 Console: `http://localhost:8082/h2-console`
