package com.mb.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Security filter to prevent HTTP Request Smuggling attacks.
 * <p>
 * This filter rejects requests that have characteristics commonly used in
 * HTTP Request Smuggling attacks, including:
 * <ul>
 *   <li>Requests with both Content-Length and Transfer-Encoding headers (CL.TE attack)</li>
 *   <li>Requests with malformed Transfer-Encoding headers</li>
 *   <li>Requests with multiple Content-Length headers with different values</li>
 * </ul>
 * <p>
 * See RFC 7230 Section 3.3.3 for the specification.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7230#section-3.3.3">RFC 7230 Section 3.3.3</a>
 * @see <a href="https://security.snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORKCLOUD-1911950">HTTP Request Smuggling</a>
 */
@Slf4j
@Component
public class HttpRequestSmugglingPreventionFilter implements WebFilter, Ordered {

    private static final String LOG_PREFIX = "HTTP Request Smuggling attempt detected:";

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        // Check for CL.TE attack: both Content-Length and Transfer-Encoding present
        if (hasContentLengthAndTransferEncoding(headers)) {
            log.warn("{} Request contains both Content-Length and Transfer-Encoding headers. URI: {}", LOG_PREFIX, exchange.getRequest().getURI());
            return rejectRequest(exchange, "Request contains both Content-Length and Transfer-Encoding headers");
        }

        // Check for multiple Content-Length headers with different values
        if (hasMultipleContentLengthValues(headers)) {
            log.warn("{} Request contains multiple Content-Length values. URI: {}", LOG_PREFIX, exchange.getRequest().getURI());
            return rejectRequest(exchange, "Request contains multiple Content-Length values");
        }

        // Check for malformed Transfer-Encoding header (includes multiple headers, whitespace tampering, comma-separated values)
        if (hasMalformedTransferEncoding(headers)) {
            log.warn("{} Request contains invalid Transfer-Encoding header. URI: {}", LOG_PREFIX, exchange.getRequest().getURI());
            return rejectRequest(exchange, "Request contains invalid Transfer-Encoding header");
        }

        return chain.filter(exchange);
    }

    /**
     * This filter runs before Spring Security's WebFilterChainProxy (which is at -100).
     * Must be very early to catch smuggling attempts before any other processing.
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean hasContentLengthAndTransferEncoding(HttpHeaders headers) {
        return headers.containsHeader(HttpHeaders.CONTENT_LENGTH) && headers.containsHeader(HttpHeaders.TRANSFER_ENCODING);
    }

    private boolean hasMultipleContentLengthValues(HttpHeaders headers) {
        var contentLengths = headers.get(HttpHeaders.CONTENT_LENGTH);
        if (contentLengths == null || contentLengths.size() <= 1) {
            return false;
        }

        // Check if all values are the same
        String firstValue = contentLengths.getFirst();
        for (String value : contentLengths) {
            if (!value.equals(firstValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMalformedTransferEncoding(HttpHeaders headers) {
        var transferEncodings = headers.get(HttpHeaders.TRANSFER_ENCODING);
        if (transferEncodings == null) {
            return false;
        }

        if (transferEncodings.size() > 1) {
            return true;
        }

        for (String encoding : transferEncodings) {
            // Check for leading/trailing whitespace (smuggling attempt)
            if (!encoding.equals(encoding.trim())) {
                return true;
            }

            // Check for multiple comma-separated values (TE.TE attack pattern)
            if (encoding.contains(",")) {
                return true;
            }

            String normalized = encoding.toLowerCase();
            // Only valid Transfer-Encoding values are allowed
            // Invalid or unknown values are rejected as potential smuggling attempts
            if (!normalized.equals("chunked") && !normalized.equals("identity") && !normalized.equals("gzip") && !normalized.equals("compress") && !normalized.equals("deflate")) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> rejectRequest(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                    "error": "Bad Request",
                    "message": "%s"
                }
                """.formatted(reason);
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
