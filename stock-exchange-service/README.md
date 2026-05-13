# Stock Exchange Service

## Description

Stock Exchange Service manages stock exchanges and stocks with **JWT-secured** REST endpoints. It provides CRUD
operations for stock exchanges, stocks, and users, with role-based access control, Redis token caching, and PostgreSQL
persistence.

## Key Features

- **JWT Authentication** — Custom JWT token-based auth with Redis-backed token store
- **Role-Based Access Control** — Fine-grained authorities (CREATE_STOCK, UPDATE_STOCK, etc.)
- **Stock Exchange CRUD** — Create, list, add/remove stocks to/from exchanges
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
- OpenTelemetry, Micrometer, Prometheus
- Testcontainers, SpringDoc OpenAPI, MapStruct, Lombok

## Configuration

| Property      | Default                   | Description             |
|---------------|---------------------------|-------------------------|
| `server.port` | `8089`                    | Service port            |
| DB            | `localhost:5432/postgres` | PostgreSQL              |
| Redis         | `localhost:6379`          | Token store             |
| Eureka        | `localhost:8761/eureka`   | Service registry        |
| OTLP          | `localhost:4318`          | OpenTelemetry collector |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8089/api/v1`
- Swagger UI: `http://localhost:8089/api/v1/swagger-ui.html`
- Actuator: `http://localhost:8089/api/v1/actuator`
- Test User: `username: admin_user` / `password: test1234`
