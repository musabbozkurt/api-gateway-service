package com.mb.modulith.localstack.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LocalStackSecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void localStackSecurityFilterChain_ShouldBeRegistered_WhenSecurityIsEnabledByDefault() {
        // Arrange
        // Act
        boolean registered = applicationContext.containsBean("localStackSecurityFilterChain");

        // Assertions
        assertThat(registered).isTrue();
        assertThat(applicationContext.getBean("localStackSecurityFilterChain")).isInstanceOf(SecurityFilterChain.class);
    }
}
