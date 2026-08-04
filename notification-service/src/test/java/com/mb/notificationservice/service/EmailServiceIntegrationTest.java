package com.mb.notificationservice.service;

import ch.martinelli.oss.testcontainers.mailpit.MailpitClient;
import ch.martinelli.oss.testcontainers.mailpit.MailpitContainer;
import ch.martinelli.oss.testcontainers.mailpit.Message;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static ch.martinelli.oss.testcontainers.mailpit.assertions.MailpitAssertions.assertThat;
import static jakarta.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class EmailServiceIntegrationTest {

    @Container
    static MailpitContainer mailpit = new MailpitContainer();

    @AfterEach
    void cleanUp() {
        mailpit.getClient().deleteAllMessages();
    }

    @Test
    void sendEmail_ShouldCatchAndVerifyEmail_WhenSimpleEmailIsSent() throws MessagingException {
        // Arrange
        String from = "sender@example.com";
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Hello, this is a test email!";

        // Act
        sendTestEmail(from, to, subject, body);

        // Assertions
        MailpitClient client = mailpit.getClient();
        List<Message> messages = client.getAllMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().subject()).isEqualTo("Test Subject");
        assertThat(messages.getFirst().attachmentCount()).isZero();
    }

    @Test
    void getMessageDetails_ShouldReturnCorrectContentAndMetadata_WhenUsingMailpitClient() throws MessagingException {
        // Arrange
        sendTestEmail("noreply@myapp.com", "user@example.com", "Welcome", "Hello User");
        MailpitClient client = mailpit.getClient();
        List<Message> messages = client.getAllMessages();
        String messageId = messages.getFirst().id();

        // Act
        int count = client.getMessageCount();
        Message message = client.getMessage(messageId);
        String plainBody = client.getMessagePlain(messageId);

        // Assertions
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(message.subject()).isEqualTo("Welcome");
        org.assertj.core.api.Assertions.assertThat(plainBody).contains("Hello User");

        // Mailpit AssertJ assertions
        assertThat(mailpit)
                .hasMessages()
                .hasMessageCount(1)
                .hasMessageWithSubject("Welcome")
                .hasMessageTo("user@example.com")
                .hasMessageFrom("noreply@myapp.com");
    }

    @Test
    void sendEmail_ShouldVerifyContainerState_WhenEmailIsSentSuccessfully() throws MessagingException {
        // Arrange
        String from = "noreply@myapp.com";
        String to = "user@example.com";
        String subject = "Welcome";
        String body = "Body text";

        // Act
        sendTestEmail(from, to, subject, body);

        // Assertions
        assertThat(mailpit)
                .hasMessages()
                .hasMessageCount(1)
                .hasMessageWithSubject("Welcome")
                .hasMessageTo("user@example.com")
                .hasMessageFrom("noreply@myapp.com");
    }

    @Test
    void verifyMessageDetails_ShouldValidateAllFields_WhenOrderConfirmationIsSent() throws MessagingException {
        // Arrange
        sendTestEmail("orders@shop.com", "customer@example.com", "Order Confirmation", "Thank you for your order");

        // Act
        // Assertions
        assertThat(mailpit)
                .firstMessage()
                .hasSubject("Order Confirmation")
                .hasSubjectContaining("Order")
                .isFrom("orders@shop.com")
                .hasRecipient("customer@example.com")
                .hasRecipientCount(1)
                .hasNoAttachments()
                .isUnread()
                .hasSnippetContaining("Thank you for your order");
    }

    @Test
    void awaitMessage_ShouldWaitForEmail_WhenAsyncPasswordResetIsTriggered() throws MessagingException {
        // Arrange
        sendTestEmail("noreply@myapp.com", "user@example.com", "Password Reset", "Click here to reset");

        // Act
        // Assertions
        assertThat(mailpit)
                .withTimeout(Duration.ofSeconds(30))
                .withPollInterval(Duration.ofSeconds(1))
                .awaitMessage()
                .withSubject("Password Reset")
                .from("noreply@myapp.com")
                .to("user@example.com")
                .isPresent()
                .hasSnippetContaining("Click here to reset");
    }

    @Test
    void awaitMessageCount_ShouldWaitForMultipleEmails_WhenBulkEmailsAreSent() throws MessagingException {
        // Arrange
        // Act
        sendTestEmail("sender@example.com", "user1@example.com", "Subject 1", "Body 1");
        sendTestEmail("sender@example.com", "user2@example.com", "Subject 2", "Body 2");
        sendTestEmail("sender@example.com", "user3@example.com", "Subject 3", "Body 3");

        // Assertions
        assertThat(mailpit)
                .withTimeout(Duration.ofSeconds(10))
                .awaitMessageCount(3);
    }

    @Test
    void filterMessages_ShouldCorrectlyGroupAndFilter_WhenDiverseEmailsExist() throws MessagingException {
        // Arrange
        sendTestEmail("newsletter@company.com", "user@example.com", "Newsletter 1", "Body");
        sendTestEmail("newsletter@company.com", "user@example.com", "Newsletter 2", "Body");
        sendTestEmail("support@company.com", "admin@example.com", "Support Ticket", "Body");
        sendTestEmail("noreply@company.com", "admin@example.com", "System Alert", "Body");
        sendTestEmail("noreply@company.com", "admin@example.com", "Backup Log", "Body");

        // Act
        // Assertions
        assertThat(mailpit)
                .messages()
                .hasSize(5)
                .filteredOnSubject("Newsletter 1")
                .hasSize(1);

        assertThat(mailpit)
                .messages()
                .filteredOnSender("support@company.com")
                .hasSize(1);

        assertThat(mailpit)
                .messages()
                .filteredOnRecipient("admin@example.com")
                .hasSize(3);

        assertThat(mailpit)
                .messages()
                .hasMessageSatisfying(0, msg -> msg.hasSubject("Backup Log").isUnread());
    }

    @Test
    void verifyAddress_ShouldValidateNameAndDomain_WhenSupportEmailIsSent() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        Properties props = new Properties();
        props.put("mail.smtp.host", mailpit.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("support@company.com", "Company Support"));
        message.setRecipient(TO, new InternetAddress("user@example.com"));
        message.setSubject("Help");
        message.setText("Support Text");

        // Act
        Transport.send(message);

        // Assertions
        assertThat(mailpit)
                .firstMessage()
                .fromAddress()
                .hasAddress("support@company.com")
                .hasName("Company Support")
                .hasDisplayName()
                .isInDomain("company.com");
    }

    @Test
    void awaitMessage_ShouldBeAbsent_WhenNoEmailIsSentToTargetRecipient() {
        // Arrange
        // Act
        // Assertions
        assertThat(mailpit)
                .awaitMessage()
                .to("unknown@example.com")
                .isAbsent();
    }

    @Test
    void sendEmail_ShouldIncludeAttachment_WhenEmailWithFileIsSent() throws MessagingException {
        // Arrange
        Properties props = new Properties();
        props.put("mail.smtp.host", mailpit.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("sender@example.com");
        helper.setTo("recipient@example.com");
        helper.setSubject("Invoice Report");
        helper.setText("Please find your attached invoice.");

        byte[] samplePdfContent = "Dummy PDF Content".getBytes();
        DataSource dataSource = new ByteArrayDataSource(samplePdfContent, "application/pdf");
        helper.addAttachment("invoice.pdf", dataSource);

        // Act
        Transport.send(message);

        // Assertions
        MailpitClient client = mailpit.getClient();
        List<Message> messages = client.getAllMessages();
        Message capturedMessage = messages.getFirst();

        assertThat(messages).hasSize(1);
        assertThat(capturedMessage.attachmentCount()).isEqualTo(1);

        assertThat(mailpit)
                .firstMessage()
                .hasSubject("Invoice Report")
                .hasRecipient("recipient@example.com")
                .hasAttachments();
    }

    @Test
    void sendEmail_ShouldHaveNoAttachments_WhenStandardPlainTextMessageIsSent() throws MessagingException {
        // Arrange
        String from = "info@company.com";
        String to = "client@example.com";
        String subject = "Plain Text Update";
        String body = "This email strictly contains text and no files.";

        // Act
        sendTestEmail(from, to, subject, body);

        // Assertions
        assertThat(mailpit)
                .firstMessage()
                .hasSubject("Plain Text Update")
                .hasNoAttachments();
    }

    private void sendTestEmail(String from, String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", mailpit.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(mailpit.getSmtpPort()));
        props.put("mail.smtp.localhost", "localhost");
        props.put("mail.from", "noreply@localhost");

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
