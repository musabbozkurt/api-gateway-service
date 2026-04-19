package com.mb.notificationservice.client.firebase.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.mb.notificationservice.client.firebase.service.FcmService;
import com.mb.notificationservice.config.FirebaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final FirebaseConfig firebaseConfig;

    @Override
    public void send(Message message, String application) {
        try {
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(firebaseConfig.getFirebaseApp(application));
            log.info("Message sent successfully for application: {}. Response: {}", application, firebaseMessaging.send(message));
        } catch (Exception e) {
            log.error("Exception occurred while sending FCM message for application: {}. Exception: {}", application, ExceptionUtils.getStackTrace(e));
        }
    }
}
