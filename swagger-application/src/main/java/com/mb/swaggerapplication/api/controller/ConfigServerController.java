package com.mb.swaggerapplication.api.controller;

import com.mb.swaggerapplication.config.ConfigServerProperties;
import com.mb.swaggerapplication.context.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes config server properties sourced from the Spring Cloud Config Server.
 *
 * <p>Annotated with {@link org.springframework.cloud.context.config.annotation.RefreshScope} so that
 * this bean is destroyed and recreated on the next access after a refresh is triggered via:
 * <ul>
 *   <li>{@code POST /actuator/refresh} — refreshes {@code @RefreshScope} beans on <b>this instance only</b></li>
 *   <li>{@code POST /actuator/busrefresh} — broadcasts a {@code RefreshRemoteApplicationEvent} over
 *       Spring Cloud Bus (Kafka) to refresh <b>all instances</b></li>
 * </ul>
 *
 * <p>{@code @Value} fields (e.g. {@code service.name}, {@code service.first-url}) are re-injected
 * from the refreshed {@link org.springframework.core.env.Environment} when the bean is recreated.
 *
 * <p>{@link com.mb.swaggerapplication.config.ConfigServerProperties} uses {@code @ConfigurationProperties}
 * and does <b>not</b> need {@code @RefreshScope} — it is automatically rebound by
 * {@code ConfigurationPropertiesRebinder} on every {@code RefreshEvent}.
 */
@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
class ConfigServerController {

    private final ConfigServerProperties configServerProperties;

    @Value("${service.name:null}")
    private String serviceName;

    @Value("${service.first-url:null}")
    private String firstUrl;

    @GetMapping("/service-name")
    public String getServiceName() {
        log.info("Details: serviceName: {}, configServerName: {}, firstUrl: {}, configServerFirstUrl: {}, username: {}, clientId: {}",
                serviceName,
                configServerProperties.getName(),
                firstUrl,
                configServerProperties.getFirstUrl(),
                ContextHolder.getContext().username(),
                ContextHolder.getContext().clientId()
        );
        return serviceName + " - " +
                configServerProperties.getName() + " - " +
                firstUrl + " - " +
                configServerProperties.getFirstUrl() + " - " +
                ContextHolder.getContext().username() + " - " +
                ContextHolder.getContext().clientId();
    }
}
