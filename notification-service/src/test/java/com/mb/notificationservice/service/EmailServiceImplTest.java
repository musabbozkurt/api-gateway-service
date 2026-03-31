package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.impl.EmailServiceImpl;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailServiceImpl;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @Mock
    private ThymeleafTemplateService thymeleafTemplateService;

    @BeforeEach
    void init() {
        emailServiceImpl = new EmailServiceImpl(javaMailSender, notificationTemplateService, thymeleafTemplateService);
        ReflectionTestUtils.setField(emailServiceImpl, "emailFrom", "sender@test.com");
        ReflectionTestUtils.setField(emailServiceImpl, "subjectPrefix", "Prefix: ");
    }

    @Test
    void send_ShouldSendEmail_WhenAllFieldsAreValid() {
        NotificationRequest request = createValidRequest();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailServiceImpl.send(request);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void send_ShouldNotSendEmail_WhenSubjectIsBlankOrNull(String subject) {
        NotificationRequest request = createValidRequest();
        request.setSubject(subject);

        emailServiceImpl.send(request);

        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenRecipientsIsEmpty() {
        NotificationRequest request = createValidRequest();
        request.setRecipients(new HashSet<>());

        emailServiceImpl.send(request);

        verifyNoInteractions(javaMailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@"})
    void send_ShouldNotSendEmail_WhenRecipientEmailIsInvalid(String invalidEmail) {
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of(invalidEmail));

        emailServiceImpl.send(request);

        verifyNoInteractions(javaMailSender);
    }

    @Test
    void send_ShouldReturnFailedResponse_WhenMailSenderFails() {
        NotificationRequest request = createValidRequest();
        doThrow(new MailSendException("Mail server error")).when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        NotificationResponse response = emailServiceImpl.send(request);

        assertFalse(response.isSuccess());
    }

    @Test
    void send_ShouldSendEmailWithResolvedTemplate_WhenTemplateCodeIsProvided() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Welcome [[${name}]]!");
        template.setBody("Hello [[${name}]]!");

        NotificationRequest request = createTemplateRequest(Map.of("name", "John"));

        when(notificationTemplateService.findActiveByCode("WELCOME_EMAIL", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(eq("Welcome [[${name}]]!"), anyMap())).thenReturn("Welcome John!");
        when(thymeleafTemplateService.processTemplate(eq("Hello [[${name}]]!"), anyMap())).thenReturn("Hello John!");
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailServiceImpl.send(request);

        verify(javaMailSender).send(any(MimeMessage.class));
        verify(notificationTemplateService).findActiveByCode("WELCOME_EMAIL", NotificationChannel.EMAIL);
    }

    private NotificationRequest createValidRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setRecipients(Set.of("to@test.com"));
        request.setCc(Set.of("cc@test.com"));
        request.setBcc(Set.of("bcc@test.com"));
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        return request;
    }

    private NotificationRequest createTemplateRequest(Map<String, Object> parameters) {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setRecipients(Set.of("to@test.com"));
        request.setCc(Set.of("cc@test.com"));
        request.setBcc(Set.of("bcc@test.com"));
        request.setTemplateCode("WELCOME_EMAIL");
        request.setTemplateParameters(parameters);
        return request;
    }
}
