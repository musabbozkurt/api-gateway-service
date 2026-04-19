# API Gateway

## Description

API Gateway is the single entry point for all client requests in the microservice architecture. Built with **Spring
Cloud Gateway** (reactive/WebFlux), it routes traffic to downstream services, provides centralized security, rate
limiting, request/response logging, and aggregated Swagger documentation.

## Key Features

- **Dynamic Routing** — Routes requests to `student-service`, `payment-service`, `openai-service`,
  `notification-service`, and `swagger-application` via Eureka service discovery (`lb://` URIs)
- **Rate Limiting** — Redis-backed `RequestRateLimiter` filter (40 requests/sec replenish, 80 burst capacity)
- **Google reCAPTCHA** — AOP-based captcha validation with `@RequiresCaptcha` annotation
- **Request/Response Logging** — Global filters for logging request and response bodies with configurable max body size
- **HTTP Request Smuggling Prevention** — Dedicated filter to block smuggling attacks
- **Authentication Filter** — Gateway-level authentication with context propagation
- **Centralized Swagger UI** — Aggregates OpenAPI docs from all downstream services at a single URL
- **Spring Cloud Eureka Client** — Registers with Eureka for service discovery
- **Micrometer Tracing** — Distributed tracing with 100% sampling for development

## Tech Stack

- Java 25, Spring Boot 4.0, Spring Cloud Gateway
- Spring WebFlux (reactive)
- Redis (rate limiting)
- Spring Cloud Eureka Client
- SpringDoc OpenAPI (aggregated Swagger UI)
- Micrometer Tracing (Brave)

## Configuration

| Property              | Default                 | Description                |
|-----------------------|-------------------------|----------------------------|
| `server.port`         | `8080`                  | Gateway port               |
| `spring.data.redis.*` | `localhost:6379`        | Redis for rate limiting    |
| `google.recaptcha.*`  | Configured via `.env`   | reCAPTCHA site/secret keys |
| `eureka.client.*`     | `localhost:8761/eureka` | Eureka service registry    |

## Routes

| Route                  | Path Prefix        | Target Service              |
|------------------------|--------------------|-----------------------------|
| `student-service`      | `/student/**`      | `lb://student-service`      |
| `swagger-application`  | `/swagger/**`      | `lb://swagger-application`  |
| `payment-service`      | `/payment/**`      | `lb://payment-service`      |
| `openai-service`       | `/openai/**`       | `lb://openai-service`       |
| `notification-service` | `/notification/**` | `lb://notification-service` |

## Running

```bash
./mvnw spring-boot:run
```

**URLs:**

- Gateway: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator`
