# OpenAI Service

## Description

OpenAI Service provides a REST API wrapper around the **OpenAI Completions API**. It uses Feign Client to communicate
with OpenAI and exposes endpoints for generating text completions.

## Key Features

- **OpenAI Completions API** — Feign Client integration with `https://api.openai.com/v1`
- **API Key Authentication** — Configurable API key via environment variable
- **Custom Error Decoder** — Feign error decoder for OpenAI error responses
- **Spring Cloud Eureka Client** — Service discovery registration

## Tech Stack

- Java 25, Spring Boot 4.0
- OpenFeign (HTTP client)
- SpringDoc OpenAPI

## Configuration

| Property                             | Default                 | Description                    |
|--------------------------------------|-------------------------|--------------------------------|
| `server.port`                        | `8084`                  | Service port                   |
| `feign.services.openai-client.token` | `YOUR_API_KEY_HERE`     | OpenAI API key (set in `.env`) |
| Eureka                               | `localhost:8761/eureka` | Service registry               |

## Running

1. Set your OpenAI API key in the `.env` file:
   ```
   YOUR_API_KEY_HERE=sk-...
   ```

2. Start the service:
   ```bash
   ./mvnw spring-boot:run
   ```

- Application: `http://localhost:8084`
- Swagger UI: `http://localhost:8084/swagger-ui/index.html`
