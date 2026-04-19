package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.DeviceTokenRequest;
import com.mb.notificationservice.data.entity.DeviceToken;

import java.util.List;
import java.util.Set;

public interface DeviceTokenService {

    void register(DeviceTokenRequest request);

    List<DeviceToken> getActiveTokensByUserIdAndApplications(Long userId, Set<String> applications);
}
