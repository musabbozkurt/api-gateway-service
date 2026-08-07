package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.config.EmailAttachmentProperties;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender javaMailSender;

    private EmailAttachmentProperties emailAttachmentProperties;

    @BeforeEach
    void init() {
        emailAttachmentProperties = new EmailAttachmentProperties();
        emailService = new EmailServiceImpl(javaMailSender, emailAttachmentProperties);
        ReflectionTestUtils.setField(emailService, "emailFrom", "sender@test.com");
        ReflectionTestUtils.setField(emailService, "subjectPrefix", "Prefix: ");
    }

    @Test
    void send_ShouldSendEmail_WhenAllFieldsAreValid() {
        // Arrange
        NotificationRequest request = createValidRequest();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        emailService.send(request);

        // Assertions
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenOptionalFieldsAreEmpty() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setCc(new HashSet<>());
        request.setBcc(new HashSet<>());
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        emailService.send(request);

        // Assertions
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenCcAndBccAreNull() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setCc(null);
        request.setBcc(null);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        emailService.send(request);

        // Assertions
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenMultipleAttachmentsAreProvided() {
        // Arrange
        NotificationRequest event = createValidRequest();
        event.setAttachments(List.of(
                createAttachment("invoice.pdf", "application/pdf", "pdf-content"),
                createAttachment("report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "excel-content")
        ));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(event);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmailWithValidAttachmentsOnly_WhenAttachmentCountExceedsLimit() {
        // Arrange
        emailAttachmentProperties.setMaxCount(1);
        NotificationRequest request = createValidRequest();
        request.setAttachments(List.of(createAttachment("a.pdf", "application/pdf", "a"), createAttachment("b.pdf", "application/pdf", "b")));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmailWithoutAttachment_WhenAttachmentContentTypeIsNotAllowed() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setAttachments(List.of(createAttachment("malware.exe", "application/octet-stream", "evil")));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void send_ShouldNotSendEmail_WhenSubjectIsBlankOrNull(String subject) {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setSubject(subject);

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void send_ShouldNotSendEmail_WhenBodyIsBlankOrNull(String body) {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setBody(body);

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenRecipientsIsNull() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setRecipients(null);

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenRecipientsIsEmpty() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setRecipients(new HashSet<>());

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenRecipientEmailIsInvalid(String invalidEmail) {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of(invalidEmail));

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenCcEmailIsInvalid(String invalidEmail) {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setCc(Set.of(invalidEmail));

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenBccEmailIsInvalid(String invalidEmail) {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setBcc(Set.of(invalidEmail));

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenRecipientsHasMixOfValidAndInvalidEmails() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of("valid@test.com", "invalid-email"));

        // Act
        emailService.send(request);

        // Assertions
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldSendEmail_WhenInlineImageAttachmentIsProvided() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setBody("<p>Welcome!</p><img src=\"cid:company-logo\"/>");
        request.setAttachments(List.of(createInlineAttachment("logo.png", "png-content", "company-logo")));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenMultipleInlineImagesAreProvided() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setBody("<p>Welcome!</p><img src=\"cid:company-logo\"/><img src=\"cid:promo-banner\"/>");
        request.setAttachments(List.of(
                createInlineAttachment("logo.png", "logo-content", "company-logo"),
                createInlineAttachment("banner.png", "banner-content", "promo-banner")
        ));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenInlineAndRegularAttachmentsAreProvided() {
        // Arrange
        NotificationRequest request = createValidRequest();
        request.setBody("<p>Invoice attached.</p><img src=\"cid:company-logo\"/>");
        request.setAttachments(List.of(
                createAttachment("invoice.pdf", "application/pdf", "pdf-content"),
                createInlineAttachment("logo.png", "png-content", "company-logo")
        ));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertTrue(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void send_ShouldReturnFailedResponse_WhenMailSenderFails() {
        // Arrange
        NotificationRequest request = createValidRequest();
        doThrow(new MailSendException("Mail server error")).when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        // Act
        NotificationResponse response = emailService.send(request);

        // Assertions
        assertFalse(response.isSuccess());
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    private NotificationRequest createValidRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setRecipients(Set.of("to@test.com"));
        request.setCc(Set.of("cc@test.com"));
        request.setBcc(Set.of("bcc@test.com"));
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        request.setAttachments(new ArrayList<>());
        return request;
    }

    private AttachmentDto createAttachment(String filename, String contentType, String content) {
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename(filename);
        attachment.setContentType(contentType);
        attachment.setContentBase64(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
        return attachment;
    }

    private AttachmentDto createInlineAttachment(String filename, String content, String contentId) {
        AttachmentDto attachment = createAttachment(filename, "image/png", content);
        attachment.setContentId(contentId);
        return attachment;
    }
}
