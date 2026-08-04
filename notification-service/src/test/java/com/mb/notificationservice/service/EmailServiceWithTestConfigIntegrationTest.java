package com.mb.notificationservice.service;

import ch.martinelli.oss.testcontainers.mailpit.MailpitClient;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import com.mb.notificationservice.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/// Reference: [Mailpit](https://testcontainers.com/modules/mailpit/)
@SpringBootTest(classes = TestcontainersConfiguration.class, properties = {"spring.cloud.config.enabled=false"})
class EmailServiceWithTestConfigIntegrationTest {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailpitClient client;

    @Test
    void shouldSendAndVerifyEmail() {
        // Use the auto-configured JavaMailSender
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sender@example.com");
        message.setTo("recipient@example.com");
        message.setSubject("Test Subject");
        message.setText("Hello, this is a test email!");

        mailSender.send(message);

        // Verify using the auto-configured MailpitClient
        List<Message> messages = client.getAllMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().subject()).isEqualTo("Test Subject");
        assertThat(messages.getFirst().from().address()).isEqualTo("sender@example.com");
        assertThat(messages.getFirst().to().getFirst().address()).isEqualTo("recipient@example.com");
    }
}
