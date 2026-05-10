package com.mb.gitlabservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GitlabServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(GitlabServiceApplication.class, args);
    }
}
