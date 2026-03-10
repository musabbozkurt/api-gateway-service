package com.mb.springconfigserver.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration
public class PostgresIntegrationConfiguration {

    public static final PostgreSQLContainer container = new PostgreSQLContainer("postgres:18.3")
            .withReuse(true);

    static {
        container.start();

        System.setProperty("spring.datasource.url", container.getJdbcUrl());
        System.setProperty("spring.datasource.username", container.getUsername());
        System.setProperty("spring.datasource.password", container.getPassword());
    }
}
