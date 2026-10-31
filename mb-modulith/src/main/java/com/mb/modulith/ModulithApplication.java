package com.mb.modulith;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableBatchProcessing
@EnableFeignClients(basePackages = "com.mb.modulith.commerce")
public class ModulithApplication {

    static void main(String[] args) {
        SpringApplication.run(ModulithApplication.class, args);
    }
}
