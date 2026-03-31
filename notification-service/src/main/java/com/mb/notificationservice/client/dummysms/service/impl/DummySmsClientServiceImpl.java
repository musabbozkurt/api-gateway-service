package com.mb.notificationservice.client.dummysms.service.impl;

import com.mb.notificationservice.client.dummysms.service.DummySmsClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DummySmsClientServiceImpl implements DummySmsClientService {

    @Override
    public void sendSms(String phoneNumber, String message) {
        // DummySmsClient.sendOtp(authorization, date, request)
        log.info("Dummy SMS client - sending SMS to: {}, message: {}", phoneNumber, message);
    }
}
