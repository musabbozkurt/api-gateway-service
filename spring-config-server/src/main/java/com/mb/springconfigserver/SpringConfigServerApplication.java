package com.mb.springconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigServer
@SpringBootApplication
public class SpringConfigServerApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringConfigServerApplication.class, args);
    }
}
