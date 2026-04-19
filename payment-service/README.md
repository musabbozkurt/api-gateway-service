# Payment Service

## Description

Payment Service handles payment processing with **Keycloak-secured** REST endpoints. It provides CRUD operations for
payments, integrates with Keycloak for OAuth2/JWT authentication, and persists payment data in PostgreSQL.

## Key Features

- **Keycloak Integration** — OAuth2 Resource Server with JWT validation
- **Multiple Swagger Auth Flows** — Password flow, authorization code, implicit, bearer, and OpenID Connect
- **Payment CRUD** — Create, read, update, and list payment records
- **PostgreSQL Persistence** — `payment_schema` with Flyway migrations
- **Spring Cloud Eureka Client** — Service discovery registration

## Tech Stack

- Java 25, Spring Boot 4.0, Spring Security (OAuth2 + JWT)
- Spring Data JPA + PostgreSQL + Flyway
- Keycloak, SpringDoc OpenAPI, MapStruct

## Configuration

| Property      | Default                   | Description       |
|---------------|---------------------------|-------------------|
| `server.port` | `8083`                    | Service port      |
| DB            | `localhost:5433/postgres` | PostgreSQL        |
| Keycloak      | `localhost:9090`          | Identity provider |
| Eureka        | `localhost:8761/eureka`   | Service registry  |

## Running

```bash
./mvnw spring-boot:run
```

- Application: `http://localhost:8083`
- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
