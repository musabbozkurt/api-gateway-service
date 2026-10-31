package com.mb.gitlabservice.client.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class GitlabFeignConfig {

    private final GitlabConfigProperties gitlabConfigProperties;

    @Bean
    public RequestInterceptor gitlabFeignRequestInterceptor() {
        return new GitlabClientRequestInterceptor(gitlabConfigProperties);
    }

    @Bean
    public ErrorDecoder gitlabErrorDecoder() {
        return new GitlabErrorDecoder();
    }

    //    @Bean
    //    public Retryer gitlabRetryer() {
    //        return new GitlabFeignRetryer();
    //    }
}
