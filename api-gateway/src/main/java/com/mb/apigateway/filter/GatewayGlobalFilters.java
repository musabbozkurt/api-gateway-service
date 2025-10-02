package com.mb.apigateway.filter;

import com.mb.apigateway.context.ContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

import static com.mb.apigateway.constant.GatewayServiceConstants.MDC_CONTEXT;

@Slf4j
@Configuration
public class GatewayGlobalFilters {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public GlobalFilter a() {
        return (exchange, chain) -> {
            MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));
            log.info("first pre filter. Path: {}", exchange.getRequest().getURI().getPath());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));
                log.info("first post filter");
            }));
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    public GlobalFilter b() {
        return (exchange, chain) -> {
            MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));
            log.info("second pre filter. Path: {}", exchange.getRequest().getURI().getPath());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));
                log.info("second post filter");
            }));
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 4)
    public GlobalFilter c() {
        return (exchange, chain) -> {
            MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));
            log.info("third pre filter. Path: {}", exchange.getRequest().getURI().getPath());

            return chain.filter(exchange)
                    .doFinally(signalType -> {
                                MDC.setContextMap(exchange.getAttribute(MDC_CONTEXT));

                                log.info("third post filter - MDC and ContextHolder cleared");
                                // Clear MDC and ContextHolder after processing
                                MDC.clear();
                                ContextHolder.clear();
                            }
                    );
        };
    }
}
