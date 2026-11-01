package com.mb.modulith.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring scheduling for all modules (delivery notifier, local stack jobs, ...).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

}
