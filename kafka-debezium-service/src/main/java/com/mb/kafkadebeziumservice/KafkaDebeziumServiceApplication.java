package com.mb.kafkadebeziumservice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaDebeziumServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(KafkaDebeziumServiceApplication.class, args);
    }
}
