# Stock Exchange Service

## Description

Stock Exchange Service manages stock exchanges, stocks, and users with **JWT-secured** REST endpoints. It serves as the
**centralized authentication provider** for both itself and inventory-management-service, issuing JWT tokens with
embedded roles. It also provides CRUD operations for stock exchanges and stocks, Kafka event streaming, and PostgreSQL
persistence.

## Key Features

- **Centralized JWT Authentication** — Issues JWT tokens (with `roles` claim) used by both stock-exchange-service and
  inventory-management-service
- **Token Introspection Endpoint** — RFC 7662 compatible `/api/v1/auth/introspect` used by API Gateway for token
  validation
- **Role-Based Access Control** — Fine-grained authorities (CREATE_STOCK, GET_PRODUCT, CREATE_CATEGORY, ADMIN, etc.)
- **User Management** — User registration, role assignment, and credential management
- **Stock Exchange CRUD** — Create, list, add/remove stocks to/from exchanges
- **Kafka Event Streaming** — Spring Cloud Stream binder for publishing events (e.g., `UserCreatedEvent`) on transaction
  commit
- **PostgreSQL Persistence** — `stock_exchange_service` schema with Flyway migrations
- **Hibernate Envers** — Entity audit/revision history
- **Redis Caching** — Token storage with Jedis client
- **Observability (LGTM)** — OpenTelemetry logs, metrics, and traces exported to Grafana LGTM stack
- **Testcontainers** — Integration tests with PostgreSQL containers
- **Spring Cloud Eureka Client** — Service discovery registration
- **MapStruct** — Type-safe DTO ↔ entity mapping

## Tech Stack

- Java 25, Spring Boot 4.0, Spring WebMVC, Spring Security
- Spring Data JPA + PostgreSQL + Flyway + Hibernate Envers
- Spring Data Redis + Jedis, JJWT
- Spring Cloud Stream + Kafka
- OpenTelemetry, Micrometer, Prometheus
- Testcontainers, SpringDoc OpenAPI, MapStruct, Lombok

## Configuration

| Property      | Default                   | Description             |
|---------------|---------------------------|-------------------------|
| `server.port` | `8089`                    | Service port            |
| DB            | `localhost:5432/postgres` | PostgreSQL              |
| Redis         | `localhost:6379`          | Token store             |
| Kafka         | `localhost:9092`          | Event streaming         |
| Eureka        | `localhost:8761/eureka`   | Service registry        |
| OTLP          | `localhost:4318`          | OpenTelemetry collector |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8089/api/v1`
- Swagger UI: `http://localhost:8089/api/v1/swagger-ui.html`
- Actuator: `http://localhost:8089/api/v1/actuator`
- Via Gateway: `http://localhost:8080/stock-exchange/**`
- Auth: `POST http://localhost:8080/stock-exchange/api/v1/auth/signin` (user: `admin_user` / password: `test1234`)
