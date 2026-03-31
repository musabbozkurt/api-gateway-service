package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.client.dummysms.service.DummySmsClientService;
import com.mb.notificationservice.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsServiceImpl smsService;

    @Mock
    private DummySmsClientService dummySmsClientService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(smsService, "bodyPrefix", "bodyPrefix");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5554443322", "05554443322", "905554443322", "+905554443322"})
    void send_ShouldSendSms_WhenGsmNumberAndMessageValid(String gsm) {
        NotificationRequest request = createRequest(gsm, "Deneme");

        smsService.send(request);

        verify(dummySmsClientService, times(1)).sendSms(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void send_ShouldReturnFailure_WhenMessageIsEmpty() {
        NotificationRequest request = createRequest("5554443322", "");

        NotificationResponse response = smsService.send(request);

        assertThat(response.isSuccess()).isFalse();
        verifyNoInteractions(dummySmsClientService);
    }

    @Test
    void send_ShouldReturnSuccess_WhenRequestIsValid() {
        NotificationRequest request = createRequest("5554443322", "Valid message");

        NotificationResponse response = smsService.send(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getChannel()).isEqualTo(NotificationChannel.SMS);
    }

    private NotificationRequest createRequest(String gsm, String message) {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setRecipients(Set.of(gsm));
        request.setBody(message);
        return request;
    }
}
