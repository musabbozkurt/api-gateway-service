package com.mb.modulith.school.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "limits")
public class LimitsConfigProperties {
    private int maximumNumberOfStudents;
    private int maximumNumberOfCourses;
}
