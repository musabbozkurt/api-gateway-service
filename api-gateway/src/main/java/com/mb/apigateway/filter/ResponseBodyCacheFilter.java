package com.mb.apigateway.filter;

import com.mb.apigateway.constant.GatewayServiceConstants;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class ResponseBodyCacheFilter implements GlobalFilter, Ordered {

    private static final Set<String> ERRORS = Set.of("GENERAL-0001");

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();

        if (isFileResponse(originalResponse)) {
            return chain.filter(exchange);
        }

        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            @NonNull
            @Override
            public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                    return super.writeWith(
                            fluxBody.buffer()
                                    .map(dataBuffers -> {
                                                DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                                                byte[] content = new byte[joinedBuffer.readableByteCount()];
                                                joinedBuffer.read(content);
                                                DataBufferUtils.release(joinedBuffer);

                                                String responseBody = new String(content, StandardCharsets.UTF_8);
                                                exchange.getAttributes().put(GatewayServiceConstants.RESPONSE_BODY_CONTAINS_ANY_ERROR, StringUtils.hasText(responseBody) && ERRORS.stream().anyMatch(responseBody::contains));

                                                return bufferFactory.wrap(content);
                                            }
                                    )
                    );
                }
                return super.writeWith(body);
            }

            @NonNull
            @Override
            public Mono<Void> writeAndFlushWith(@NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * This filter runs after {@link AuthenticationFilter} which has {@link Ordered#HIGHEST_PRECEDENCE} + 1,
     * and before {@link LoggingFilter} to cache response body first.
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 2
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    private boolean isFileResponse(ServerHttpResponse response) {
        // Check the response headers for any indication that the response is a file
        // For example, you can check the "Content-Disposition" header
        String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
        return contentDisposition != null && contentDisposition.startsWith("attachment");
    }
}
