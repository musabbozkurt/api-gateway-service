# Student Service

## Description

Student Service manages student data and demonstrates inter-service communication patterns including **Feign Client with
Keycloak OAuth2**, **HCaptcha integration**, and **event-driven messaging** via RabbitMQ.

## Key Features

- **Student CRUD** — In-memory H2 database for student records
- **Feign Client with OAuth2** — Secure service-to-service calls to Payment Service using Keycloak client credentials
- **HCaptcha Integration** — Four different HCaptcha validation implementations (Feign, RestTemplate, WebClient, manual)
- **Event-Driven Messaging** — RabbitMQ producer/consumer with Spring Cloud Stream
- **Dead Letter Queue** — Automatic DLQ with TTL for failed messages
- **Spring Cloud Eureka Client** — Service discovery registration

## Tech Stack

- Java 25, Spring Boot 4.0
- Spring Data JPA + H2 (in-memory)
- Spring Cloud Stream + RabbitMQ
- OpenFeign + OAuth2 (Keycloak client credentials)
- SpringDoc OpenAPI

## Configuration

| Property      | Default                 | Description           |
|---------------|-------------------------|-----------------------|
| `server.port` | `8081`                  | Service port          |
| RabbitMQ      | `localhost:5673`        | Message broker        |
| Keycloak      | `localhost:9090`        | OAuth2 token provider |
| Eureka        | `localhost:8761/eureka` | Service registry      |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- H2 Console: `http://localhost:8081/h2-console`
