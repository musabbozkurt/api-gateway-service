# Elasticsearch TLS for user activity publishing

`UserActivityPublisherServiceImpl` sends user API activity events to Elasticsearch over a dedicated reactive
`WebClient`.
This guide explains common TLS failures and how to fix them in Vault or in code.

## Symptoms

| `base-url`                                | Typical log message                                                      | Cause                                                                                               |
|-------------------------------------------|--------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `http://<host>:9200`                      | `PrematureCloseException: Connection prematurely closed BEFORE response` | Cluster listens on **HTTPS only**; plain HTTP is dropped.                                           |
| `https://<host>:9200`                     | `PKIX path building failed` / `unable to find valid certification path`  | Server sends **only the leaf certificate**, not the intermediate CA the JVM needs to build a chain. |
| `https://<ip>:9200` (after PKIX is fixed) | `No subject alternative names matching IP address`                       | Certificate SANs list **DNS names only**, not the IP.                                               |

**Goal:** `https://<es-host>.<your-domain>:9200` with a verifiable chain (or an explicit non-production trust-all
setting).

---

## DevOps (preferred)

### 1. Inspect the certificate

```bash
openssl s_client -connect <es-host>:9200 </dev/null 2>/dev/null | openssl x509 -noout -issuer -ext subjectAltName
```

Note the issuer (public CA vs internal CA) and which host names appear in `subjectAltName`.

### 2. Send the full chain from Elasticsearch

Configure each node so the HTTP TLS listener serves **server certificate + intermediate** (not the leaf alone).
Use your platform’s usual keystore or `elasticsearch.yml` certificate settings, then rolling-restart nodes.

### 3. DNS

If the certificate has no IP SAN, create internal DNS:

```text
<es-host>.<your-domain>  ->  <elasticsearch-ip>
```

Confirm resolution from the gateway pod:

```bash
nslookup <es-host>.<your-domain>
```

### 4. Verify before changing the gateway

```bash
# Expect verify return code 0 (or HTTP 401/404 from curl without -k)
openssl s_client -connect <es-host>.<your-domain>:9200 -servername <es-host>.<your-domain> </dev/null
curl -I -u <user>:<pass> https://<es-host>.<your-domain>:9200/
```

### 5. Vault

```yaml
user-activity:
  elasticsearch:
    enabled: true
    base-url: https://<es-host>.<your-domain>:9200
    ssl-certificate-verified: true
```

Restart api-gateway and confirm index warnings stop and `user-activity-*` indices receive documents.

---

## Application behaviour (api-gateway)

Configuration is bound by `UserActivityElasticsearchProperties` (`user-activity.elasticsearch.*`).

| `ssl-certificate-verified` | `base-url` scheme | Client behaviour                                                                                                             |
|----------------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------|
| `true` (default)           | `https`           | JDK truststore **plus** bundled intermediates in `src/main/resources/elasticsearch/es-ca.crt`. Hostname verification **on**. |
| `true`                     | `http`            | Plain HTTP (typical local dev).                                                                                              |
| `false`                    | `https`           | Trust any certificate; hostname verification **off** (Logstash `ssl_certificate_verified` style). **Not for production.**    |
| `false`                    | `http`            | Plain HTTP.                                                                                                                  |

Only the `user-activity-es` connection pool uses these TLS settings. Gateway routing and other outbound calls are
unchanged.

### When DevOps fixes the chain (Option A)

No code change. Use `ssl-certificate-verified: true` and a `base-url` host name that matches the certificate.

### Bundled CA file (Option B)

The bundle is always loaded for `https` + `ssl-certificate-verified: true`, so no extra setting is needed. If the
intermediate cannot be configured on Elasticsearch, maintain `es-ca.crt` (PEM, one or more intermediate CAs; each block
is commented with its expiry date). Replace the file when your CA vendor rotates intermediates or before they expire.

### Trust all (Option C)

```yaml
user-activity:
  elasticsearch:
    base-url: https://<es-host>.<your-domain>:9200   # an IP also works here, since hostname checks are skipped
    ssl-certificate-verified: false
```

Use only for dev/test or as a short-lived workaround.

---

## Tests

```bash
cd api-gateway-service/api-gateway
./mvnw test -Dtest=UserActivityPublisherServiceTest
```

TLS cases use a **temporary** self-signed keystore created in `@BeforeAll` (`@TempDir` + `keytool`), not a committed
`.p12`.

---

## Checklist

- [ ] Certificate issuer and SANs documented
- [ ] Full chain on Elasticsearch **or** `es-ca.crt` updated in the app
- [ ] DNS name matches certificate (if no IP SAN)
- [ ] `curl` / `openssl` succeed without `-k`
- [ ] Vault `base-url` uses `https` and the correct host name
- [ ] Gateway logs show no `User activity index initialization ... failed`
