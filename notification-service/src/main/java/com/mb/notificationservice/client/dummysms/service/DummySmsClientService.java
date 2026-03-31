package com.mb.notificationservice.client.dummysms.service;

public interface DummySmsClientService {

    void sendSms(String phoneNumber, String message);
}
