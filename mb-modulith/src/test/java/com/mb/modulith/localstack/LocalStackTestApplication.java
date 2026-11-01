package com.mb.modulith.localstack;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Slim Spring Boot test application for the local stack module.
 * It is found first by {@code @SpringBootTest} (closest {@code @SpringBootConfiguration} up the package tree), so the
 * integration tests only load this module and do not need PostgreSQL, Redis, RabbitMQ or the other modules.
 * Like the original standalone application it does not configure a relational datasource.
 * Marked as {@link TestConfiguration} so that the full {@code ModulithApplication} component scan skips it and its
 * auto-configuration exclusions do not leak into the full context.
 */
@TestConfiguration
@EnableScheduling
@EnableFeignClients
@SpringBootApplication(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
public class LocalStackTestApplication {

}
