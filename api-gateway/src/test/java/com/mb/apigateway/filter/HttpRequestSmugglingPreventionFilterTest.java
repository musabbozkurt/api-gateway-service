package com.mb.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpRequestSmugglingPreventionFilterTest {

    @Nested
    @DisplayName("CL.TE Attack Prevention Tests")
    class ClTeAttackPreventionTest {

        private HttpRequestSmugglingPreventionFilter filter;
        private WebFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new HttpRequestSmugglingPreventionFilter();
            chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @Test
        @DisplayName("Request with both Content-Length and Transfer-Encoding should be rejected with 400")
        void filter_ShouldReturnBadRequest_WhenBothContentLengthAndTransferEncodingPresent() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/backend-service/api/v1/products")
                    .header(HttpHeaders.CONTENT_LENGTH, "3")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains both Content-Length and Transfer-Encoding headers");

            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("HTTP Request Smuggling attack should be rejected with 400")
        void filter_ShouldReturnBadRequest_WhenSmugglingAttackDetected() {
            // Arrange - This simulates the attack from the original request
            MockServerHttpRequest request = MockServerHttpRequest.post("/backend-service/api/v1/products")
                    .header(HttpHeaders.CONTENT_LENGTH, "3")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains both Content-Length and Transfer-Encoding headers");

            verify(chain, never()).filter(any());
        }
    }

    @Nested
    @DisplayName("Multiple Content-Length Prevention Tests")
    class MultipleContentLengthPreventionTest {

        private HttpRequestSmugglingPreventionFilter filter;
        private WebFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new HttpRequestSmugglingPreventionFilter();
            chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @Test
        @DisplayName("Request with multiple different Content-Length values should be rejected with 400")
        void filter_ShouldReturnBadRequest_WhenMultipleDifferentContentLengthValues() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.CONTENT_LENGTH, "10")
                    .header(HttpHeaders.CONTENT_LENGTH, "20")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains multiple Content-Length values");

            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Request with same Content-Length value repeated should be allowed")
        void filter_ShouldContinueChain_WhenSameContentLengthValueRepeated() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.CONTENT_LENGTH, "10")
                    .header(HttpHeaders.CONTENT_LENGTH, "10")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Malformed Transfer-Encoding Prevention Tests")
    class MalformedTransferEncodingPreventionTest {

        private HttpRequestSmugglingPreventionFilter filter;
        private WebFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new HttpRequestSmugglingPreventionFilter();
            chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                // Malformed patterns
                "chunked, identity, chunked",  // Comma-separated values
                " chunked",                     // Leading whitespace
                "chunked ",                     // Trailing whitespace
                // Invalid encoding values
                "invalid",           // Unknown encoding
                "chunk",             // Typo/variation of chunked
                "chunke",            // Partial match
                "chunkedd",          // Extra character
                "unknown",           // Arbitrary unknown value
                "base64",            // Not a valid transfer encoding
                "br",                // Brotli (not valid for Transfer-Encoding)
                "zstd",              // Zstandard (not valid for Transfer-Encoding)
                "x-custom",          // Custom/proprietary encoding
                "trailers",          // Not a valid Transfer-Encoding value
                "keep-alive"         // Connection header value, not Transfer-Encoding
        })
        @DisplayName("Request with invalid or malformed Transfer-Encoding should be rejected with 400")
        void filter_ShouldReturnBadRequest_WhenInvalidOrMalformedTransferEncoding(String transferEncodingValue) {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.TRANSFER_ENCODING, transferEncodingValue)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");

            verify(chain, never()).filter(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "chunked",    // Most common valid value
                "identity",   // No encoding
                "gzip",       // Gzip compression
                "compress",   // Unix compress
                "deflate",    // Deflate compression
                "CHUNKED",    // Uppercase chunked (case-insensitive)
                "IDENTITY",   // Uppercase identity
                "GZIP",       // Uppercase gzip
                "COMPRESS",   // Uppercase compress
                "DEFLATE",    // Uppercase deflate
                "Chunked",    // Mixed case
                "GZip"        // Mixed case
        })
        @DisplayName("Request with valid Transfer-Encoding value should be allowed")
        void filter_ShouldContinueChain_WhenValidTransferEncodingValue(String transferEncodingValue) {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.TRANSFER_ENCODING, transferEncodingValue)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Request with multiple Transfer-Encoding headers should be rejected (TE.TE attack)")
        void filter_ShouldReturnBadRequest_WhenMultipleTransferEncodingHeaders() {
            // Arrange
            // This tests the hasMultipleTransferEncodingHeaders method (transferEncodings.size() > 1)
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.TRANSFER_ENCODING, "identity")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(chain, never()).filter(any());

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");
        }

        @Test
        @DisplayName("Request with three Transfer-Encoding headers should be rejected")
        void filter_ShouldReturnBadRequest_WhenThreeTransferEncodingHeaders() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.TRANSFER_ENCODING, "gzip")
                    .header(HttpHeaders.TRANSFER_ENCODING, "identity")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");

            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("Request with duplicate chunked Transfer-Encoding headers should be rejected")
        void filter_ShouldReturnBadRequest_WhenDuplicateChunkedTransferEncodingHeaders() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/test")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Verify response body
            String responseBody = exchange.getResponse().getBodyAsString().block();
            assertThat(responseBody)
                    .contains("Bad Request")
                    .contains("Request contains invalid Transfer-Encoding header");

            verify(chain, never()).filter(any());
        }
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTest {

        private HttpRequestSmugglingPreventionFilter filter;
        private WebFilterChain chain;

        @BeforeEach
        void setUp() {
            filter = new HttpRequestSmugglingPreventionFilter();
            chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
        }

        @Test
        @DisplayName("Normal POST request with only Content-Length should be allowed")
        void filter_ShouldContinueChain_WhenOnlyContentLength() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/backend-service/api/v1/products")
                    .header(HttpHeaders.CONTENT_LENGTH, "100")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("GET request without body headers should be allowed")
        void filter_ShouldContinueChain_WhenGetRequestWithoutBodyHeaders() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/rbac-service/api/v1/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Request without any body-related headers should be allowed")
        void filter_ShouldContinueChain_WhenNoBodyHeaders() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = filter.filter(exchange, chain);

            // Assertions
            StepVerifier.create(result).verifyComplete();
            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Filter Order Tests")
    class FilterOrderTest {

        @Test
        @DisplayName("Filter order should be HIGHEST_PRECEDENCE to run before Spring Security")
        void getOrder_ShouldReturnHighestPrecedence_WhenCalled() {
            // Arrange
            HttpRequestSmugglingPreventionFilter filter = new HttpRequestSmugglingPreventionFilter();

            // Act
            int order = filter.getOrder();

            // Assertions
            assertThat(order).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        }
    }
}
