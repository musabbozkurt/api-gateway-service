package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.client.dummysms.service.DummySmsClientService;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.util.SmsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements NotificationStrategy {

    private final DummySmsClientService dummySmsClientService;

    @Value("${dummy-sms.body-prefix:}")
    private String bodyPrefix;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        String id = UUID.randomUUID().toString();

        try {
            SmsUtils.validate(request);

            String phoneNumber = request.getRecipients().iterator().next();
            String message = bodyPrefix + request.getBody();
            dummySmsClientService.sendSms(phoneNumber, message);

            log.info("SMS notification sent successfully. Id: {}, Recipient: {}", id, request.getRecipients());

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.SMS)
                    .success(true)
                    .message("SMS sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred while sending SMS notification. Id: {}, Recipient: {}, Exception: {}", id, request.getRecipients(), ExceptionUtils.getStackTrace(e));

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.SMS)
                    .success(false)
                    .message("Failed to send SMS: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
