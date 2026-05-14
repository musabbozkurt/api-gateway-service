# Brokerage Provider

## Description

Brokerage Provider handles brokerage order processing with reactive REST endpoints. It provides CRUD operations for
orders, integrates with Kafka for event-driven messaging, and persists data in PostgreSQL with Hibernate Envers
auditing.

## Key Features

- **Reactive WebFlux** — Non-blocking REST API with Spring WebFlux
- **Kafka Integration** — Producer/consumer for order events
- **PostgreSQL Persistence** — `brokerage_provider` schema with Flyway migrations
- **Hibernate Envers** — Entity audit/revision history
- **Observability (LGTM)** — OpenTelemetry logs, metrics, and traces exported to Grafana LGTM stack
- **Prometheus Metrics** — `/actuator/prometheus` endpoint for pull-based scraping
- **Testcontainers** — Integration tests with PostgreSQL and Kafka containers
- **Spring Cloud Eureka Client** — Service discovery registration
- **MapStruct** — Type-safe DTO ↔ entity mapping

## Tech Stack

- Java 25, Spring Boot 4.0, Spring WebFlux
- Spring Data JPA + PostgreSQL + Flyway + Hibernate Envers
- Spring Kafka, MapStruct, Lombok
- OpenTelemetry, Micrometer, Prometheus
- Testcontainers, SpringDoc OpenAPI

## Configuration

| Property      | Default                   | Description             |
|---------------|---------------------------|-------------------------|
| `server.port` | `8088`                    | Service port            |
| DB            | `localhost:5432/postgres` | PostgreSQL              |
| Kafka         | `localhost:9092`          | Event broker            |
| Eureka        | `localhost:8761/eureka`   | Service registry        |
| OTLP          | `localhost:4318`          | OpenTelemetry collector |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8088`
- Swagger UI: `http://localhost:8088/swagger-ui.html`
- Actuator: `http://localhost:8088/actuator`
