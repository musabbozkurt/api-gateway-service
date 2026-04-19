# Spring Config Server

## Description

Spring Config Server provides **centralized externalized configuration** for all microservices. It supports multiple
configuration backends (JDBC + Git) and uses **Spring Cloud Bus with Kafka** to broadcast configuration changes in
real-time.

## Key Features

- **Dual Config Backend** — JDBC (H2, priority 1) + Git (GitHub repository, priority 2)
- **Spring Cloud Bus** — Kafka-based event bus to broadcast `RefreshRemoteApplicationEvent` to all connected services
- **Config Change Watcher** — Polls the database for configuration changes and auto-broadcasts refresh events
- **Flyway Migrations** — Schema management for the config properties table
- **H2 Console** — Web-based database console for development
- **Spring Cloud Eureka Client** — Service discovery registration

## Tech Stack

- Java 25, Spring Boot 4.0
- Spring Cloud Config Server
- Spring Cloud Bus + Kafka
- H2 (in-memory database) + Flyway
- Spring Cloud Eureka Client

## Configuration

| Property                                 | Default                                       | Description              |
|------------------------------------------|-----------------------------------------------|--------------------------|
| `server.port`                            | `8888`                                        | Config server port       |
| `config.change-watcher.poll-interval-ms` | `5000`                                        | DB polling interval (ms) |
| Git URI                                  | `github.com/musabbozkurt/api-gateway-service` | Git config source        |
| Kafka                                    | `localhost:9092`                              | Spring Cloud Bus broker  |
| Eureka                                   | `localhost:8761/eureka`                       | Service registry         |

## Endpoints

```http
# Fetch config for a service
GET /{application}/{profile}/{label}
# Example: GET /swagger-application/development/main

# Refresh this instance only
POST /actuator/refresh

# Broadcast refresh to all instances via Kafka
POST /actuator/busrefresh
```

## Running

```bash
./mvnw spring-boot:run
```

- Config Server: `http://localhost:8888`
- Health: `http://localhost:8888/actuator/health`
- H2 Console: `http://localhost:8888/h2-console`
