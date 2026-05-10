package com.mb.gitlabservice.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feign.services.gitlab-client")
public class GitlabConfigProperties {
    private String url;
    private String privateToken;
    private String pathWithNamespace;
    private long maxNumberOfProject;
}
