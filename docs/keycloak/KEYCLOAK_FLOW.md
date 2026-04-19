# Keycloak Authentication & Authorization Flow

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT (Browser / Mobile / Postman)            │
│                                                                             │
│  1. POST /realms/payment-service/protocol/openid-connect/token              │
│     ─────────────────────────────────────┐                                  │
│                                          │                                  │
│  4. Use access_token in Authorization    │                                  │
│     header for all API calls             │                                  │
└──────────────┬───────────────────────────┼──────────────────────────────────┘
               │                           │
               │ Bearer Token              │ grant_type=password
               │                           │ (or client_credentials)
               ▼                           ▼
┌──────────────────────┐        ┌─────────────────────────┐
│                      │        │                         │
│    API Gateway       │        │      Keycloak           │
│    (port 8080)       │        │      (port 9090)        │
│                      │        │                         │
│  ┌────────────────┐  │        │  ┌───────────────────┐  │
│  │ SecurityConfig │  │        │  │ payment-service   │  │
│  │                │  │  2.    │  │ realm             │  │
│  │ Opaque Token   │──┼───────►│  │                   │  │
│  │ Introspection  │  │Verify  │  │ • Clients         │  │
│  │ (commented out)│◄─┼────────│  │ • Users           │  │
│  └────────────────┘  │  3.    │  │ • Roles           │  │
│                      │Result  │  │ • RS256 keys      │  │
│  Routes requests to  │        │  └───────────────────┘  │
│  downstream services │        │                         │
└──────┬───────────────┘        └────────────▲────────────┘
       │                                     │
       │ Forwards request                    │ JWT validation
       │ with Bearer token                   │ (jwk-set-uri)
       │                                     │
       ▼                                     │
┌──────────────────────────────────────────────────────────┐
│                   DOWNSTREAM SERVICES                    │
│                                                          │
│  ┌─────────────────┐  ┌───────────────────────────────┐  │
│  │ payment-service │  │ student-service               │  │
│  │ (port 8083)     │  │ (port 8081)                   │  │
│  │                 │  │                               │  │
│  │ JWT Resource    │  │ OAuth2 Client (Feign)         │  │
│  │ Server         ─┼──┤ client_credentials grant      │  │
│  │                 │  │ to call payment-service       │  │
│  │ JwtAuthConverter│  │ via API Gateway               │  │
│  │ extracts roles  │  │                               │  │
│  │ from            │  │ SecurityFilterChain:          │  │
│  │ resource_access │  │ permitAll (no JWT validation) │  │
│  └─────────────────┘  └───────────────────────────────┘  │
│                                                          │
│  ┌──────────────────┐  ┌──────────────────────────────┐  │
│  │ notification-svc │  │ openai-service               │  │
│  │ (port 8085)      │  │ (port 8084)                  │  │
│  │ No Keycloak      │  │ No Keycloak                  │  │
│  │ (internal/Kafka) │  │ (secured at gateway level)   │  │
│  └──────────────────┘  └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

## How It Works

### 1. Keycloak Setup (Identity Provider)

Keycloak runs on **port 9090** and manages a realm called **`payment-service`**. The realm contains:

| Concept       | Description                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------|
| **Realm**     | `payment-service` — an isolated tenant with its own users, clients, and roles                 |
| **Client**    | `payment-service` — a confidential OAuth2 client with a `client-secret`                       |
| **Users**     | End-users who authenticate with username/password                                             |
| **Roles**     | Realm and client-level roles (e.g., `client_admin`) mapped to JWT `resource_access` claims    |
| **Endpoints** | Token, introspection, JWKS, userinfo under `/realms/payment-service/protocol/openid-connect/` |

A realm export is provided at `docs/keycloak/payment-service-realm-export.json` for quick import.

### 2. Token Acquisition

Clients obtain a JWT access token from Keycloak:

```bash
# Password Grant (end-user login)
curl -X POST http://localhost:9090/realms/payment-service/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=payment-service" \
  -d "client_secret=<CLIENT_SECRET>" \
  -d "username=<USERNAME>" \
  -d "password=<PASSWORD>"

# Client Credentials Grant (service-to-service)
curl -X POST http://localhost:9090/realms/payment-service/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=payment-service" \
  -d "client_secret=<CLIENT_SECRET>"
```

Response contains `access_token` (JWT), `refresh_token`, `expires_in` (300s default).

### 3. Request Flow Through the System

```
Client ──Bearer token──► API Gateway (8080) ──forwards──► payment-service (8083)
                              │                                    │
                              │                          Validates JWT signature
                              │                          via Keycloak JWKS endpoint
                              │                          Extracts roles from
                              │                          resource_access claim
                              │
                              ├──────────────────────► student-service (8081)
                              │                          No JWT validation
                              │                          (permitAll)
                              │
                              ├──────────────────────► notification-service (8085)
                              │                          No Keycloak integration
                              │
                              └──────────────────────► openai-service (8084)
                                                        No Keycloak integration
```

### 4. Service-Level Security Details

#### API Gateway (`api-gateway`)

- **Currently**: SecurityConfig is **commented out** — all requests pass through without token validation
- **When enabled**: Uses **Opaque Token Introspection** — sends every token to Keycloak's introspection endpoint to
  verify validity (real-time check, more secure but adds latency)
- Config: `secure-service.introspection-uri`, `gateway-service.client-id`, `gateway-service.client-secret`

#### Payment Service (`payment-service`) — **Fully Secured**

- Acts as an **OAuth2 Resource Server** with **JWT validation**
- Validates JWT signature using Keycloak's JWKS keys (`jwk-set-uri`)
- **`JwtAuthConverter`** extracts client roles from the `resource_access.<client-id>.roles` claim and maps them to
  Spring `ROLE_*` authorities
- `@EnableMethodSecurity` allows `@PreAuthorize("hasRole('client_admin')")` on controller methods
- Swagger UI supports **Password Flow** for interactive testing (configured in `SwaggerPasswordFlowConfig`)
- Swagger endpoints (`/api-docs`, `/swagger-ui/**`) are **permitted without auth**

#### Student Service (`student-service`) — **OAuth2 Client (Feign)**

- `SecurityFilterChain` is set to **`permitAll`** — does NOT validate incoming JWTs
- Uses Keycloak as an **OAuth2 Client** for outgoing Feign calls to `payment-service`
- `OAuthFeignConfig` obtains a **client_credentials** token from Keycloak and attaches it as a `Bearer` header to every
  Feign request
- This allows student-service to call payment-service endpoints that require authentication

#### Notification / OpenAI / Other Services

- No direct Keycloak integration
- Protected at the **gateway level** (when gateway security is enabled)

### 5. JWT Token Structure

A decoded Keycloak JWT contains:

```json
{
  "exp": 1713520000,
  "iss": "http://localhost:9090/realms/payment-service",
  "sub": "user-uuid",
  "resource_access": {
    "payment-service": {
      "roles": [
        "client_admin",
        "client_user"
      ]
    }
  },
  "preferred_username": "john",
  "email": "john@example.com"
}
```

`JwtAuthConverter` reads `resource_access.payment-service.roles` → maps to `ROLE_client_admin`, `ROLE_client_user`.

## What Must Be Running

| Service              | Port | Required For                                          |
|----------------------|------|-------------------------------------------------------|
| **PostgreSQL**       | 5433 | Keycloak persistence (shares DB with payment-service) |
| **Keycloak**         | 9090 | Token issuance, JWT signing keys, introspection       |
| **Service Registry** | 8761 | Service discovery (Eureka)                            |
| **API Gateway**      | 8080 | Single entry point, routes to all services            |
| **Redis**            | 6379 | Gateway rate limiting                                 |

### Startup Order

```
1. PostgreSQL          ─── database for Keycloak + services
2. Keycloak            ─── depends on PostgreSQL
3. Service Registry    ─── Eureka (independent)
4. Redis               ─── for gateway rate limiter
5. Spring Config Server ─── optional, for externalized config
6. API Gateway         ─── depends on registry, redis
7. payment-service     ─── depends on PostgreSQL, Keycloak
8. student-service     ─── depends on Keycloak (for Feign tokens)
9. Other services      ─── no Keycloak dependency
```

> **Tip**: `docker-compose up` handles this automatically via `depends_on` conditions.

## Keycloak Initial Setup (Manual)

If not using the realm export file:

1. Open http://localhost:9090 → login with admin credentials (`KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` from `.env`)
2. **Create realm** → name: `payment-service`
3. **Create client** → Client ID: `payment-service`, Client authentication: ON, set client secret
4. **Create roles** → e.g., `client_admin`, `client_user` under the client's roles tab
5. **Create users** → assign roles
6. Set `PAYMENT_CLIENT_SECRET_ENV` in `.env` to the client secret

### Import Realm Export (Recommended)

```bash
# Via Keycloak Admin REST API
curl -X POST http://localhost:9090/admin/realms \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d @docs/keycloak/payment-service-realm-export.json
```

Or import via Keycloak Admin UI → Realm Settings → Partial Import.

## Two Validation Strategies

| Strategy                       | Used By                    | How It Works                                                          | Pros                       | Cons                                     |
|--------------------------------|----------------------------|-----------------------------------------------------------------------|----------------------------|------------------------------------------|
| **JWT (local)**                | payment-service            | Downloads JWKS public keys from Keycloak, validates signature locally | Fast, no per-request call  | Can't detect revoked tokens until expiry |
| **Opaque Token Introspection** | api-gateway (when enabled) | Sends token to Keycloak introspection endpoint on every request       | Real-time revocation check | Adds latency, Keycloak must be available |

## Service-to-Service Flow (student → payment)

```
student-service                    Keycloak                    payment-service
     │                                │                              │
     │  1. client_credentials grant   │                              │
     │  ─────────────────────────────►│                              │
     │                                │                              │
     │  2. access_token (JWT)         │                              │
     │  ◄─────────────────────────────│                              │
     │                                │                              │
     │  3. GET /payment/api/v1/...    │                              │
     │     Authorization: Bearer JWT  │                              │
     │  ──────────────────────────────┼─────────────────────────────►│
     │                                │                              │
     │                                │  4. Validate JWT via JWKS    │
     │                                │◄─────────────────────────────│
     │                                │─────────────────────────────►│
     │                                │                              │
     │  5. Response                   │                              │
     │  ◄─────────────────────────────┼──────────────────────────────│
```

The `OAuthClientCredentialsFeignManager` in student-service automates steps 1–2, caching the token and refreshing it
when expired. The `RequestInterceptor` attaches it to every outgoing Feign request.
