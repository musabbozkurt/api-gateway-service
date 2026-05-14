# Inventory Management Service

## Description

Inventory Management Service manages inventory, products, and categories with **JWT-secured** REST endpoints. It
validates JWT tokens issued by **stock-exchange-service** (shared secret) and provides CRUD operations with role-based
access control, PostgreSQL persistence, and Redis caching.

## Key Features

- **JWT Token Validation** — Validates tokens issued by stock-exchange-service using a shared secret (no local user
  database or auth endpoints)
- **Role-Based Access Control** — Fine-grained authorities extracted from JWT `roles` claim (e.g., GET_PRODUCT,
  CREATE_CATEGORY)
- **Product & Category CRUD** — Full lifecycle management with category-product relationships
- **PostgreSQL Persistence** — `inventory_management_service` schema with Flyway migrations
- **Hibernate Envers** — Entity audit/revision history
- **Redis Caching** — Application-level caching with Jedis client
- **Observability (LGTM)** — OpenTelemetry logs, metrics, and traces exported to Grafana LGTM stack
- **Testcontainers** — Integration tests with PostgreSQL containers
- **Spring Cloud Eureka Client** — Service discovery registration
- **MapStruct** — Type-safe DTO ↔ entity mapping
- **JavaMoney (Moneta)** — Monetary amount handling

## Tech Stack

- Java 25, Spring Boot 4.0, Spring WebMVC, Spring Security
- Spring Data JPA + PostgreSQL + Flyway + Hibernate Envers
- Spring Data Redis + Jedis, JJWT
- OpenTelemetry, Micrometer, Prometheus
- Testcontainers, SpringDoc OpenAPI, MapStruct, Lombok

## Authentication

This service does **not** issue its own tokens. Users authenticate via **stock-exchange-service** (
`POST /stock-exchange/api/v1/auth/signin`) and use the returned JWT to access inventory APIs. Both services share the
same JWT signing secret, so the `AuthTokenFilter` validates tokens locally by verifying the signature and extracting the
username and roles from JWT claims — no database or network call required.

## Configuration

| Property      | Default                   | Description             |
|---------------|---------------------------|-------------------------|
| `server.port` | `8090`                    | Service port            |
| DB            | `localhost:5433/postgres` | PostgreSQL              |
| Redis         | `localhost:6379`          | Caching                 |
| Eureka        | `localhost:8761/eureka`   | Service registry        |
| OTLP          | `localhost:4318`          | OpenTelemetry collector |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8090`
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- Actuator: `http://localhost:8090/actuator`
- Via Gateway: `http://localhost:8080/inventory/**`
- Auth: Get token from `POST http://localhost:8080/stock-exchange/api/v1/auth/signin` (user: `admin_user` / password:
  `test1234`)
