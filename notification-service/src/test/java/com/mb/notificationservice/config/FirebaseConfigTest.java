package com.mb.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class FirebaseConfigTest {

    private FirebaseConfig firebaseConfig;
    private MockedStatic<FirebaseApp> firebaseAppMockedStatic;
    private MockedStatic<GoogleCredentials> googleCredentialsMockedStatic;

    @BeforeEach
    void setUp() {
        firebaseConfig = new FirebaseConfig();
        firebaseAppMockedStatic = mockStatic(FirebaseApp.class);
        googleCredentialsMockedStatic = mockStatic(GoogleCredentials.class);
    }

    @AfterEach
    void tearDown() {
        firebaseAppMockedStatic.close();
        googleCredentialsMockedStatic.close();
    }

    @Test
    void initialize_ShouldInitializeFirebaseApp_WhenAppNotAlreadyRegistered() {
        // Arrange
        String appName = "test-app";
        Resource resource = new ByteArrayResource("{}".getBytes());
        Map<String, Resource> apps = new HashMap<>();
        apps.put(appName, resource);
        firebaseConfig.setApps(apps);

        firebaseAppMockedStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
        googleCredentialsMockedStatic.when(() -> GoogleCredentials.fromStream(any(InputStream.class))).thenReturn(mock(GoogleCredentials.class));
        firebaseAppMockedStatic.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(appName))).thenReturn(mock(FirebaseApp.class));

        // Act
        assertDoesNotThrow(() -> firebaseConfig.initialize());

        // Assertions
        firebaseAppMockedStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(appName)));
    }

    @Test
    void initialize_ShouldSkipInitialization_WhenAppAlreadyRegistered() {
        // Arrange
        String appName = "existing-app";
        Resource resource = new ByteArrayResource("{}".getBytes());
        Map<String, Resource> apps = new HashMap<>();
        apps.put(appName, resource);
        firebaseConfig.setApps(apps);

        FirebaseApp existingApp = mock(FirebaseApp.class);
        when(existingApp.getName()).thenReturn(appName);
        firebaseAppMockedStatic.when(FirebaseApp::getApps).thenReturn(List.of(existingApp));

        // Act
        assertDoesNotThrow(() -> firebaseConfig.initialize());

        // Assertions
        firebaseAppMockedStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(appName)), never());
    }

    @Test
    void initialize_ShouldHandleException_WhenResourceIsInvalid() throws IOException {
        // Arrange
        String appName = "invalid-app";
        Resource badResource = mock(Resource.class);
        when(badResource.getInputStream()).thenThrow(new IOException("File not found"));

        Map<String, Resource> apps = new HashMap<>();
        apps.put(appName, badResource);
        firebaseConfig.setApps(apps);

        firebaseAppMockedStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());

        // Act — should not throw, error is logged
        assertDoesNotThrow(() -> firebaseConfig.initialize());

        // Assertions
        firebaseAppMockedStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(appName)), never());
    }

    @Test
    void initialize_ShouldInitializeMultipleApps_WhenMultipleAppsConfigured() {
        // Arrange
        Resource resource1 = new ByteArrayResource("{}".getBytes());
        Resource resource2 = new ByteArrayResource("{}".getBytes());
        Map<String, Resource> apps = new HashMap<>();
        apps.put("app-one", resource1);
        apps.put("app-two", resource2);
        firebaseConfig.setApps(apps);

        GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
        FirebaseApp mockApp = mock(FirebaseApp.class);

        firebaseAppMockedStatic.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
        googleCredentialsMockedStatic.when(() -> GoogleCredentials.fromStream(any(InputStream.class))).thenReturn(mockCredentials);
        firebaseAppMockedStatic.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), any(String.class))).thenReturn(mockApp);

        // Act
        assertDoesNotThrow(() -> firebaseConfig.initialize());

        // Assertions
        firebaseAppMockedStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq("app-one")));
        firebaseAppMockedStatic.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq("app-two")));
    }

    @Test
    void getFirebaseApp_ShouldReturnFirebaseAppInstance_WhenAppIsInitialized() {
        // Arrange
        String appName = "test-app";
        FirebaseApp mockApp = mock(FirebaseApp.class);
        firebaseAppMockedStatic.when(() -> FirebaseApp.getInstance(appName)).thenReturn(mockApp);

        // Act
        FirebaseApp result = firebaseConfig.getFirebaseApp(appName);

        // Assertions
        assertNotNull(result);
        assertEquals(mockApp, result);
    }

    @Test
    void getFirebaseApp_ShouldThrowException_WhenAppNotInitialized() {
        // Arrange
        String appName = "non-existent-app";
        firebaseAppMockedStatic.when(() -> FirebaseApp.getInstance(appName)).thenThrow(new IllegalStateException("FirebaseApp with name " + appName + " doesn't exist."));

        // Act
        // Assertions
        assertThrows(IllegalStateException.class, () -> firebaseConfig.getFirebaseApp(appName));
    }

    @Test
    void getApps_ShouldBeEmpty_WhenNoAppsConfigured() {
        // Assertions
        assertNotNull(firebaseConfig.getApps());
        assertTrue(firebaseConfig.getApps().isEmpty());
    }
}
