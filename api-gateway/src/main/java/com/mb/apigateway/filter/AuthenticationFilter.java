package com.mb.apigateway.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.apigateway.enums.AccessType;
import com.mb.apigateway.service.ServiceAccessCacheService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mb.apigateway.constant.GatewayServiceConstants.CLIENT_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USERNAME;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_ID;
import static com.mb.apigateway.constant.GatewayServiceConstants.USER_NAME;

/**
 * Propagates authenticated user identity to downstream services via request headers.
 * <p>
 * For authenticated requests, extracts {@code userId}, {@code username}, and {@code clientId}
 * from the {@link org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication}
 * token attributes — falling back to manual JWT payload decoding when introspection omits those claims.
 * Unauthenticated requests (permitted paths) are passed through unchanged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final ClassPathResource MAINTENANCE_PAGE = new ClassPathResource("templates/maintenance.html");
    private static final DataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();
    private static final TypeReference<HashMap<String, String>> STRING_MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ServiceAccessCacheService serviceAccessCacheService;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).then(Mono.empty()))) // no security context (pre-auth / permitted path) → pass through
                .flatMap(securityContext -> {
                    if (!(securityContext.getAuthentication() instanceof BearerTokenAuthentication bearerAuth)) {
                        return chain.filter(exchange); // anonymous / permitted path – skip access check
                    }

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(requestBuilder -> requestBuilder.headers(headers -> setHeadersFromBearerAuth(exchange, bearerAuth, headers)))
                            .build();

                    String clientId = (String) mutatedExchange.getAttributes().get(CLIENT_ID);
                    String userId = (String) mutatedExchange.getAttributes().get(USER_ID);
                    String path = exchange.getRequest().getURI().getPath();
                    String method = exchange.getRequest().getMethod().name();
                    String serviceName = extractServiceName(path);
                    String api = extractApiPath(path);
                    String accessType = StringUtils.isNotBlank(userId) ? AccessType.USER.name() : AccessType.CLIENT.name();

                    return serviceAccessCacheService.hasAccess(clientId, serviceName, api, method, accessType)
                            .flatMap(hasAccess -> {
                                if (Boolean.FALSE.equals(hasAccess)) {
                                    log.warn("Access denied for clientId: {}, service: {}, api: {}, method: {}", clientId, serviceName, api, method);
                                    return renderMaintenancePage(exchange);
                                }
                                return chain.filter(mutatedExchange);
                            });
                });
    }

    /**
     * This filter runs after {@link HttpRequestSmugglingPreventionFilter} which has {@link Ordered#HIGHEST_PRECEDENCE}.
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 1
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private void setHeadersFromBearerAuth(ServerWebExchange exchange, BearerTokenAuthentication bearerAuth, HttpHeaders headers) {
        Map<String, Object> tokenAttributes = bearerAuth.getTokenAttributes();

        String clientId = getAttribute(tokenAttributes, CLIENT_ID);
        String userId = getAttribute(tokenAttributes, USER_ID);
        String username = getAttribute(tokenAttributes, USER_NAME);

        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(userId) || StringUtils.isBlank(username)) {
            HashMap<String, String> claims = extractJwtClaims(bearerAuth.getToken().getTokenValue());

            if (StringUtils.isBlank(clientId)) {
                clientId = claims.get(CLIENT_ID);
            }

            userId = getUserId(userId, claims);

            if (StringUtils.isBlank(username)) {
                username = claims.get(USER_NAME);
            }
        }

        setHeaderAndAttribute(headers, exchange, CLIENT_ID, clientId);
        setHeaderAndAttribute(headers, exchange, USER_ID, userId);
        setHeaderAndAttribute(headers, exchange, USERNAME, username);
    }

    private String getAttribute(Map<String, Object> attributes, String key) {
        return Optional.ofNullable(attributes.get(key))
                .map(Object::toString)
                .orElse(null);
    }

    /**
     * Decodes the payload (second segment) of a JWT token to extract claims.
     * Returns an empty map if the token is not in valid JWT format (header.payload.signature)
     * or if decoding/parsing fails — e.g., opaque tokens passed by introspection-based auth.
     */
    private HashMap<String, String> extractJwtClaims(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                log.warn("Token is not a valid JWT (expected at least 2 dot-separated parts, got {})", chunks.length);
                return new HashMap<>();
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payload, STRING_MAP_TYPE);
        } catch (Exception e) {
            log.error("Error occurred while extracting JWT claims. Exception: {}", ExceptionUtils.getStackTrace(e));
            return new HashMap<>();
        }
    }

    private String getUserId(String userId, HashMap<String, String> claims) {
        if (StringUtils.isBlank(userId)) {
            Object userIdClaim = claims.get(USER_ID);
            userId = userIdClaim != null ? userIdClaim.toString() : userId;
        }
        return userId;
    }

    private void setHeaderAndAttribute(HttpHeaders headers, ServerWebExchange exchange, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            headers.set(key, value);
            exchange.getAttributes().put(key, value);
        }
    }

    private String extractServiceName(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        // Path format: /{service-name}/api/v1/...
        String[] segments = path.split("/");
        if (segments.length > 1) {
            return segments[1];
        }
        return null;
    }

    private String extractApiPath(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        // Remove the service name prefix: /{service-name}/api/... -> /api/...
        int secondSlash = path.indexOf('/', 1);
        if (secondSlash > 0) {
            return path.substring(secondSlash);
        }
        return path;
    }

    private Mono<Void> renderMaintenancePage(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);

        return DataBufferUtils.read(MAINTENANCE_PAGE, BUFFER_FACTORY, 8192)
                .collectList()
                .flatMap(dataBuffers -> exchange.getResponse().writeWith(Mono.just(BUFFER_FACTORY.join(dataBuffers))));
    }
}
