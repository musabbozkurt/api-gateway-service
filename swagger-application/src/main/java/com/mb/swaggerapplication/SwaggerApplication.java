package com.mb.swaggerapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.mb.swaggerapplication", "com.mb.gitlabservice"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mb.gitlabservice.client")
public class SwaggerApplication {

    static void main(String[] args) {
        SpringApplication.run(SwaggerApplication.class, args);
    }
}
