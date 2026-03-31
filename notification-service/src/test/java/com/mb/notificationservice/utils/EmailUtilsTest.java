package com.mb.notificationservice.utils;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.util.EmailUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "null"})
    void isValid_ShouldReturnFalse_WhenSubjectIsBlankOrNull(String subject) {
        NotificationRequest request = createValidRequest();
        request.setSubject("null".equals(subject) ? null : subject);

        assertThat(EmailUtils.isValid(request)).isFalse();
    }

    @Test
    void isValid_ShouldReturnFalse_WhenRecipientsIsNull() {
        NotificationRequest request = createValidRequest();
        request.setRecipients(null);

        assertThat(EmailUtils.isValid(request)).isFalse();
    }

    @Test
    void isValid_ShouldReturnFalse_WhenRecipientsIsEmpty() {
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of());

        assertThat(EmailUtils.isValid(request)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-to-email", "invalid@", "@invalid.com"})
    void isValid_ShouldReturnFalse_WhenRecipientsContainsInvalidEmail(String invalidEmail) {
        NotificationRequest request = createValidRequest();
        request.setRecipients(Set.of(invalidEmail));

        assertThat(EmailUtils.isValid(request)).isFalse();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenAllFieldsAreValid() {
        assertThat(EmailUtils.isValid(createValidRequest())).isTrue();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTemplateCodeProvidedWithoutSubjectAndBody() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setRecipients(Set.of("valid@example.com"));
        request.setTemplateCode("WELCOME_EMAIL");

        assertThat(EmailUtils.isValid(request)).isTrue();
    }

    private NotificationRequest createValidRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setSubject("Valid Subject");
        request.setBody("Valid Body");
        request.setRecipients(Set.of("valid@example.com"));
        request.setCc(Set.of("validcc@example.com"));
        request.setBcc(Set.of("validbcc@example.com"));
        return request;
    }
}
