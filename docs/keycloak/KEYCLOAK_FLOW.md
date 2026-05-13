# Keycloak Authentication & Authorization Flow

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT (Browser / Mobile / Postman)            │
│                                                                             │
│  1. POST /realms/mb-realm/protocol/openid-connect/token                     │
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
│  │ SecurityConfig │  │        │  │ mb-realm          │  │
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
┌──────────────────────────────────────────────────────────────────┐
│                       DOWNSTREAM SERVICES                        │
│                                                                  │
│  ┌─────────────────┐  ┌───────────────────────────────┐          │
│  │ payment-service │  │ student-service               │          │
│  │ (port 8083)     │  │ (port 8081)                   │          │
│  │                 │  │                               │          │
│  │ JWT Resource    │  │ OAuth2 Client (Feign)         │          │
│  │ Server         ─┼──┤ client_credentials grant      │          │
│  │                 │  │ to call payment-service       │          │
│  │ JwtAuthConverter│  │ via API Gateway               │          │
│  │ extracts roles  │  │                               │          │
│  │ from            │  │ SecurityFilterChain:          │          │
│  │ resource_access │  │ permitAll (no JWT validation) │          │
│  └─────────────────┘  └───────────────────────────────┘          │
│                                                                  │
│  ┌──────────────────────────┐  ┌──────────────────────────────┐  │
│  │ stock-exchange-service   │  │ notification-svc             │  │
│  │ (port 8089)              │  │ (port 8085)                  │  │
│  │                          │  │ No Keycloak                  │  │
│  │ OWN JWT Auth (HS512)     │  │ (internal/Kafka)             │  │
│  │ /api/v1/auth/signin      │  └──────────────────────────────┘  │
│  │ /api/v1/auth/signup      │                                    │
│  │ Issues own tokens        │  ┌──────────────────────────────┐  │
│  │ (NOT Keycloak)           │  │ openai-service               │  │
│  │ Gateway: permitted path  │  │ (port 8084)                  │  │
│  │ (bypasses introspection) │  │ No Keycloak                  │  │
│  └──────────────────────────┘  │ (secured at gateway level)   │  │
│                                └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

## How It Works

### 1. Keycloak Setup (Identity Provider)

Keycloak runs on **port 9090** and manages a realm called **`mb-realm`**. The realm contains:

| Concept       | Description                                                                                |
|---------------|--------------------------------------------------------------------------------------------|
| **Realm**     | `mb-realm` — an isolated tenant with its own users, clients, and roles                     |
| **Client**    | `api-gateway-client` — a confidential OAuth2 client with a `client-secret`                 |
| **Users**     | End-users who authenticate with username/password                                          |
| **Roles**     | Realm and client-level roles (e.g., `client_admin`) mapped to JWT `resource_access` claims |
| **Endpoints** | Token, introspection, JWKS, userinfo under `/realms/mb-realm/protocol/openid-connect/`     |

A realm export is provided at `docs/keycloak/mb-realm-export.json` for quick import.

### 2. Token Acquisition

Clients obtain a JWT access token from Keycloak:

```bash
# Password Grant (end-user login)
curl -X POST http://localhost:9090/realms/mb-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=api-gateway-client" \
  -d "client_secret=<CLIENT_SECRET>" \
  -d "username=<USERNAME>" \
  -d "password=<PASSWORD>"

# Client Credentials Grant (service-to-service)
curl -X POST http://localhost:9090/realms/mb-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=api-gateway-client" \
  -d "client_secret=<CLIENT_SECRET>"
```

Response contains `access_token` (JWT), `refresh_token`, `expires_in` (300s default).

### 3. Request Flow Through the System

```
                        ┌── Keycloak Auth Flow ──────────────────────────────────┐
                        │                                                        │
Client ──Bearer token──►│ API Gateway (8080) ──forwards──► payment-service (8083)│
                        │      │                                    │            │
                        │      │                          Validates JWT signature│
                        │      │                          via Keycloak JWKS      │
                        │      │                          endpoint               │
                        │      │                                                 │
                        │      ├──────────────────────► student-service (8081)   │
                        │      │                          No JWT validation      │
                        │      │                          (permitAll)            │
                        │      │                                                 │
                        │      ├──────────────────────► notification-svc (8085)  │
                        │      │                          No Keycloak            │
                        │      │                                                 │
                        │      └──────────────────────► openai-service (8084)    │
                        │                                 No Keycloak            │
                        └────────────────────────────────────────────────────────┘

                        ┌── Stock Exchange Auth Flow (Own JWT) ──────────────────┐
                        │                                                        │
Client ──signin req────►│ API Gateway (8080) ──forwards──► stock-exchange (8089) │
                        │   (permitted path,                     │               │
                        │    no introspection)           Own AuthTokenFilter     │
                        │                                validates HS512 JWT     │
Client ◄─ JWT token ────│◄──────────────────────────────────────│                │
                        │                                                        │
Client ──Bearer token──►│ API Gateway (8080) ──forwards──► stock-exchange (8089) │
                        │   (permitted path)              Validates own JWT      │
                        │                                 Extracts roles from DB │
                        └────────────────────────────────────────────────────────┘
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

#### Stock Exchange Service (`stock-exchange-service`) — **Own JWT Auth (Not Keycloak)**

- Uses its **own authentication system** — NOT Keycloak
- Users sign up/sign in via `/api/v1/auth/signup` and `/api/v1/auth/signin`
- Issues **HS512-signed JWTs** from its own `AuthTokenFilter`
- Stores users/roles in its own database (H2/PostgreSQL)
- **Gateway validates tokens** via `TokenIntrospectionGatewayFilterFactory` — calls `/api/v1/auth/introspect` on
  stock-exchange-service before forwarding authenticated requests
- Exposes `/api/v1/auth/introspect` endpoint (RFC 7662 compatible) that returns `{active: true/false, username, roles}`
- Auth/swagger/actuator paths bypass both Keycloak introspection and token introspection filter
- Role-based access control via `@PreAuthorize` annotations (e.g., `CREATE_STOCK_EXCHANGE`, `ADD_STOCK`)

**Token acquisition:**

```bash
# Sign in to stock-exchange-service (via gateway)
curl -X POST http://localhost:8080/stock-exchange/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username": "admin_user", "password": "test1234"}'

# Response:
# {
#   "token": "eyJhbGciOiJIUzUxMiJ9...",
#   "type": "Bearer",
#   "username": "admin_user",
#   "roles": ["ADD_STOCK", "CREATE_STOCK", ...]
# }

# Use the token for authenticated requests
curl -X GET http://localhost:8080/stock-exchange/api/v1/stock-exchanges \
  -H "Authorization: Bearer <STOCK_EXCHANGE_TOKEN>"
```

**Gateway token validation flow:**

```
Client ──Bearer token──► API Gateway ──introspect──► stock-exchange /api/v1/auth/introspect
                              │                              │
                              │            {active: true}    │
                              │◄─────────────────────────────│
                              │                              │
                              │──────forwards request───────►│
```

### 5. JWT Token Structure

A decoded Keycloak JWT contains:

```json
{
  "exp": 1713520000,
  "iss": "http://localhost:9090/realms/mb-realm",
  "sub": "user-uuid",
  "resource_access": {
    "api-gateway-client": {
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

`JwtAuthConverter` reads `resource_access.api-gateway-client.roles` → maps to `ROLE_client_admin`, `ROLE_client_user`.

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
2. ****Create realm** → name: `mb-realm`
3. ****Create client** → Client ID: `api-gateway-client`, Client authentication: ON, set client secret
4. **Create roles** → e.g., `client_admin`, `client_user` under the client's roles tab
5. **Create users** → assign roles
6. Set `GATEWAY_CLIENT_SECRET` in `.env` to the client secret

### Import Realm Export (Recommended)

```bash
# Via Keycloak Admin REST API
curl -X POST http://localhost:9090/admin/realms \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d @docs/keycloak/mb-realm-export.json
```

Or import via Keycloak Admin UI → Realm Settings → Partial Import.

## Three Validation Strategies

| Strategy                        | Used By                      | How It Works                                                                   | Pros                                    | Cons                                     |
|---------------------------------|------------------------------|--------------------------------------------------------------------------------|-----------------------------------------|------------------------------------------|
| **JWT (local, Keycloak)**       | payment-service              | Downloads JWKS public keys from Keycloak, validates signature locally          | Fast, no per-request call               | Can't detect revoked tokens until expiry |
| **Opaque Token Introspection**  | api-gateway (Keycloak)       | Sends token to Keycloak introspection endpoint on every request                | Real-time revocation check              | Adds latency, Keycloak must be available |
| **Service Token Introspection** | api-gateway → stock-exchange | Gateway calls stock-exchange `/api/v1/auth/introspect` to validate service JWT | Validates at gateway level, centralized | Adds one extra hop per request           |

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
