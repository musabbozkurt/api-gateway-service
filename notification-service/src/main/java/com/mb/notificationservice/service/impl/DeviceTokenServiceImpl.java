package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.DeviceTokenRequest;
import com.mb.notificationservice.data.entity.DeviceToken;
import com.mb.notificationservice.data.repository.DeviceTokenRepository;
import com.mb.notificationservice.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    @Transactional
    public void register(DeviceTokenRequest request) {
        Long userId = ContextHolder.getContext().userId();
        deviceTokenRepository.findByUserIdAndApplication(userId, request.application())
                .ifPresentOrElse(deviceToken -> {
                            deviceToken.setToken(request.token());
                            deviceToken.setPlatform(request.platform());
                            deviceToken.setActive(true);
                            deviceTokenRepository.save(deviceToken);
                        }, () -> deviceTokenRepository.save(convert(request, userId))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceToken> getActiveTokensByUserIdAndApplications(Long userId, Set<String> applications) {
        return deviceTokenRepository.findByUserIdAndActiveIsTrueAndApplicationIn(userId, applications);
    }

    private DeviceToken convert(DeviceTokenRequest request, Long userId) {
        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setUserId(userId);
        deviceToken.setToken(request.token());
        deviceToken.setPlatform(request.platform());
        deviceToken.setApplication(request.application());
        deviceToken.setActive(true);
        return deviceToken;
    }
}
