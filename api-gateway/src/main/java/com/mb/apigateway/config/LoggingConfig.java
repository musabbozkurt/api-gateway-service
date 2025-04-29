package com.mb.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "logging.request")
public class LoggingConfig {

    /**
     * Enable or disable request logging
     */
    private boolean enabled = true;

    /**
     * Enable or disable request body logging
     */
    private boolean includeRequestBody = true;

    /**
     * Enable or disable response body logging
     */
    private boolean includeResponseBody = true;

    /**
     * Maximum body size to log (in bytes)
     */
    private int maxBodySize = 10000;

    /**
     * List of paths to exclude from logging
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * List of content types to exclude from body logging
     */
    private List<String> excludeContentTypes = new ArrayList<>();

    /**
     * List of file extensions to exclude from body logging
     */
    private List<String> excludeFileExtensions = List.of("jpg", "jpeg", "png", "gif", "bmp", "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "zip", "tar", "gz", "rar");
}
