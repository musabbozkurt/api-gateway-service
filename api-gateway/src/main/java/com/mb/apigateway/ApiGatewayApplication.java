package com.mb.apigateway;

import com.mb.apigateway.utils.ElasticsearchCaBundleGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApiGatewayApplication.class);
        // Runs once, before any bean (incl. the Elasticsearch client) is created.
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            String path = event.getEnvironment().getProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY);
            if (path != null && !path.isBlank()) {
                System.setProperty(ElasticsearchCaBundleGenerator.BUNDLE_PATH_PROPERTY, path);
            }
            ElasticsearchCaBundleGenerator.generateIfMissing(event.getEnvironment().getProperty("user-activity.elasticsearch.base-url"));
        });
        application.run(args);
    }

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }
}
