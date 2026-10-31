# MB Modulith

Single Spring Modulith application merging school registration, clinic, player market, email, food warehouse,
commerce, and delivery APIs. This module lives under the [`api-gateway-service`](../README.md) monorepo.

## Prerequisites

- Java 26 and Maven (or use `./mvnw`) for local runs
- Docker for infrastructure and optional containerized app

## Run locally

Start PostgreSQL, Redis, and RabbitMQ:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Build and test:

```bash
./mvnw clean install
./mvnw test
```

Swagger UI: http://localhost:8080/ (redirects to Swagger UI) or http://localhost:8080/swagger-ui/index.html

Use **Authorize** in Swagger with HTTP Basic (e.g. seeded user `TUIMUSEMENT` / `test1234` from Postman env) to call
secured APIs.

## Docker

The `Dockerfile` uses a multi-stage build (Maven + `jlink` custom runtime on Debian slim). Tests are skipped during
`docker build` because Testcontainers needs a Docker daemon inside the build context; run `./mvnw test` on the host
before building.

Build the image:

```bash
docker build -t mb-modulith:latest .
```

Run infrastructure only (same as local dev):

```bash
docker compose up -d
```

From the **api-gateway-service** root, start the full stack (includes `mb-modulith` on port **8090**):

```bash
docker compose --profile start_application up -d --build
```

- Direct Swagger UI: http://localhost:8090/swagger-ui/index.html
- Via API Gateway (path prefix `/modulith`, first segment
  stripped): http://localhost:8080/modulith/swagger-ui/index.html

Run the app module only with local `docker-compose.yaml` (profile `app`):

```bash
docker compose --profile app up -d --build
```

The `app` service sets `SPRING_PROFILES_ACTIVE=docker` and connects to `postgres`, `redis`, and `rabbitmq` via
[`application-docker.yml`](src/main/resources/application-docker.yml). Swagger UI: http://localhost:8080/

Run a standalone container (you must provide reachable Postgres/Redis/RabbitMQ URLs):

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e POSTGRES_HOST=host.docker.internal \
  -e REDIS_HOST=host.docker.internal \
  -e RABBITMQ_HOST=host.docker.internal \
  mb-modulith:latest
```

## Modules

| Module        | Package                        | Example paths                          |
|---------------|--------------------------------|----------------------------------------|
| School        | `com.mb.modulith.school`       | `/students`, `/courses`                |
| Clinic        | `com.mb.modulith.clinic`       | `/patient`, `/doctor`, `/appointments` |
| Player market | `com.mb.modulith.playermarket` | `/teams`, `/players`                   |
| Email         | `com.mb.modulith.email`        | `/client/emails/**`                    |
| Warehouse     | `com.mb.modulith.warehouse`    | `/api/foods`                           |
| Commerce      | `com.mb.modulith.commerce`     | `/api/auth`, `/api/orders`             |
| Delivery      | `com.mb.modulith.delivery`     | `/api/person`, `/api/delivery`         |

## Postman

Collections live under [`docs/postman/`](docs/postman/).

## Tests

```bash
./mvnw test
```

Test methods follow `methodName_Should..._When...` with `// Arrange`, `// Act`, `// Assertions` sections.
