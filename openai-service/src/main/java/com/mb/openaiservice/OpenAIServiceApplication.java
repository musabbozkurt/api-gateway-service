package com.mb.openaiservice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenAIServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(OpenAIServiceApplication.class, args);
    }
}
