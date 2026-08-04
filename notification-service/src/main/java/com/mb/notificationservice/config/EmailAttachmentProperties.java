package com.mb.notificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "email.attachments")
public class EmailAttachmentProperties {

    private int maxCount = 5;
    private long maxFileSizeBytes = 5_242_880L;
    private long maxTotalSizeBytes = 15_728_640L;

    private Set<String> allowedContentTypes = new LinkedHashSet<>(Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "text/csv",
            "text/plain",
            "image/png",
            "image/jpeg"
    ));
}
