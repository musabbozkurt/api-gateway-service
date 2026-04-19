# Service Registry

## Description

Service Registry is a **Spring Cloud Eureka Server** that provides service discovery for all microservices in the
architecture. Each service registers itself with Eureka and discovers other services by name.

## Key Features

- **Eureka Server** — Central service registry for all microservices
- **Self-Preservation Mode** — Built-in resilience for network partitions
- **Dashboard** — Web UI to monitor registered services

## Tech Stack

- Java 25, Spring Boot 4.0
- Spring Cloud Netflix Eureka Server

## Configuration

| Property                             | Default | Description              |
|--------------------------------------|---------|--------------------------|
| `server.port`                        | `8761`  | Eureka server port       |
| `eureka.client.register-with-eureka` | `false` | Does not register itself |
| `eureka.client.fetch-registry`       | `false` | Does not fetch registry  |

## Running

```bash
./mvnw spring-boot:run
```

- Eureka Dashboard: `http://localhost:8761/`
