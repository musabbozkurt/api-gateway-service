package com.mb.inventorymanagementservice.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Slf4j
@TestConfiguration
public class TestRedisConfiguration {

    private final RedisServer redisServer;

    public TestRedisConfiguration() throws IOException {
        this.redisServer = new RedisServer(6379);
    }

    @PostConstruct
    public void postConstruct() {
        try {
            redisServer.start();
        } catch (Exception e) {
            log.error("Exception occurred while starting redisServer. postConstruct - Exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    @PreDestroy
    public void preDestroy() {
        try {
            redisServer.stop();
        } catch (Exception e) {
            log.error("Exception occurred while stopping redisServer. preDestroy - Exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
