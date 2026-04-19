package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.DeviceTokenRequest;
import com.mb.notificationservice.data.entity.DeviceToken;
import com.mb.notificationservice.data.repository.DeviceTokenRepository;
import com.mb.notificationservice.enums.DevicePlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceImplTest {

    private static final Long USER_ID = 12345L;
    private static final String APP_ONE = "app-one";
    private static final String APP_TWO = "app-two";

    @InjectMocks
    private DeviceTokenServiceImpl deviceTokenService;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Captor
    private ArgumentCaptor<DeviceToken> deviceTokenCaptor;

    @BeforeEach
    void setUp() {
        ContextHolder.clear();
        ContextHolder.setContext(ContextHolder.Context.builder().userId(USER_ID).build());
    }

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void register_ShouldCreateNewDeviceToken_WhenNoExistingTokenForUser() {
        // Arrange
        DeviceTokenRequest request = new DeviceTokenRequest("new-fcm-token", DevicePlatform.ANDROID, APP_ONE);

        when(deviceTokenRepository.findByUserIdAndApplication(USER_ID, APP_ONE)).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deviceTokenService.register(request);

        // Assertions
        verify(deviceTokenRepository).save(deviceTokenCaptor.capture());
        DeviceToken saved = deviceTokenCaptor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals("new-fcm-token", saved.getToken());
        assertEquals(DevicePlatform.ANDROID, saved.getPlatform());
        assertEquals(APP_ONE, saved.getApplication());
        assertTrue(saved.isActive());
    }

    @Test
    void register_ShouldUpdateExistingToken_WhenTokenAlreadyExistsForUser() {
        // Arrange
        DeviceToken existing = new DeviceToken();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setToken("old-fcm-token");
        existing.setPlatform(DevicePlatform.ANDROID);
        existing.setApplication(APP_ONE);
        existing.setActive(true);

        DeviceTokenRequest request = new DeviceTokenRequest("updated-fcm-token", DevicePlatform.IOS, APP_ONE);

        when(deviceTokenRepository.findByUserIdAndApplication(USER_ID, APP_ONE)).thenReturn(Optional.of(existing));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deviceTokenService.register(request);

        // Assertions
        verify(deviceTokenRepository).save(deviceTokenCaptor.capture());
        DeviceToken saved = deviceTokenCaptor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals("updated-fcm-token", saved.getToken());
        assertEquals(DevicePlatform.IOS, saved.getPlatform());
        assertTrue(saved.isActive());
    }

    @Test
    void register_ShouldReactivateToken_WhenExistingTokenIsInactive() {
        // Arrange
        DeviceToken existing = new DeviceToken();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setToken("old-token");
        existing.setPlatform(DevicePlatform.WEB);
        existing.setApplication(APP_TWO);
        existing.setActive(false);

        DeviceTokenRequest request = new DeviceTokenRequest("reactivated-token", DevicePlatform.WEB, APP_TWO);

        when(deviceTokenRepository.findByUserIdAndApplication(USER_ID, APP_TWO)).thenReturn(Optional.of(existing));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deviceTokenService.register(request);

        // Assertions
        verify(deviceTokenRepository).save(deviceTokenCaptor.capture());
        DeviceToken saved = deviceTokenCaptor.getValue();
        assertTrue(saved.isActive());
        assertEquals("reactivated-token", saved.getToken());
    }

    @Test
    void getActiveTokensByUserIdAndApplications_ShouldReturnTokens_WhenActiveTokensExist() {
        // Arrange
        DeviceToken token1 = new DeviceToken();
        token1.setUserId(USER_ID);
        token1.setToken("fcm-token-1");
        token1.setApplication(APP_ONE);
        token1.setActive(true);

        DeviceToken token2 = new DeviceToken();
        token2.setUserId(USER_ID);
        token2.setToken("fcm-token-2");
        token2.setApplication(APP_TWO);
        token2.setActive(true);

        Set<String> applications = Set.of(APP_ONE, APP_TWO);
        when(deviceTokenRepository.findByUserIdAndActiveIsTrueAndApplicationIn(USER_ID, applications)).thenReturn(List.of(token1, token2));

        // Act
        List<DeviceToken> result = deviceTokenService.getActiveTokensByUserIdAndApplications(USER_ID, applications);

        // Assertions
        assertEquals(2, result.size());
    }

    @Test
    void getActiveTokensByUserIdAndApplications_ShouldReturnEmptyList_WhenNoActiveTokensExist() {
        // Arrange
        Set<String> applications = Set.of(APP_ONE);
        when(deviceTokenRepository.findByUserIdAndActiveIsTrueAndApplicationIn(USER_ID, applications)).thenReturn(Collections.emptyList());

        // Act
        List<DeviceToken> result = deviceTokenService.getActiveTokensByUserIdAndApplications(USER_ID, applications);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveTokensByUserIdAndApplications_ShouldReturnPartialResults_WhenOnlySomeApplicationsHaveTokens() {
        // Arrange
        DeviceToken token = new DeviceToken();
        token.setUserId(USER_ID);
        token.setToken("fcm-token-1");
        token.setApplication(APP_ONE);
        token.setActive(true);

        Set<String> applications = Set.of(APP_ONE, APP_TWO);
        when(deviceTokenRepository.findByUserIdAndActiveIsTrueAndApplicationIn(USER_ID, applications)).thenReturn(List.of(token));

        // Act
        List<DeviceToken> result = deviceTokenService.getActiveTokensByUserIdAndApplications(USER_ID, applications);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(APP_ONE, result.getFirst().getApplication());
    }
}
