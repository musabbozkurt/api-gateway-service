package com.mb.notificationservice.client.firebase.service;

import com.google.firebase.messaging.Message;

public interface FcmService {

    void send(Message message, String application);
}
