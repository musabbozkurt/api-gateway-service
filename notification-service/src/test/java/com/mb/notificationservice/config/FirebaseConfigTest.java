package com.mb.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private ResourceLoader resourceLoader;
    private MockedStatic<FirebaseApp> firebaseAppMockedStatic;
    private MockedStatic<GoogleCredentials> googleCredentialsMockedStatic;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);
        firebaseConfig = new FirebaseConfig();
        firebaseConfig.setResourceLoader(resourceLoader);
        firebaseAppMockedStatic = mockStatic(FirebaseApp.class);
        googleCredentialsMockedStatic = mockStatic(GoogleCredentials.class);
    }

    @AfterEach
    void tearDown() {
        firebaseAppMockedStatic.close();
        googleCredentialsMockedStatic.close();
    }

    @Test
    void initialize_ShouldInitializeFirebaseApp_WhenInlineJsonProvided() {
        // Arrange
        String appName = "test-app";
        String json = "{}";
        Map<String, String> apps = new HashMap<>();
        apps.put(appName, json);
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
    void initialize_ShouldInitializeFirebaseApp_WhenSpringResourceProvided() {
        // Arrange
        String appName = "resource-app";
        String resourcePath = "classpath:firebase/service-account.json";
        Resource mockResource = new ByteArrayResource("{}".getBytes());
        when(resourceLoader.getResource(resourcePath)).thenReturn(mockResource);

        Map<String, String> apps = new HashMap<>();
        apps.put(appName, resourcePath);
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
    void initialize_ShouldInitializeFirebaseApp_WhenFilePathProvided(@TempDir Path tempDir) throws Exception {
        // Arrange
        String appName = "filepath-app";
        Path tempFile = tempDir.resolve("service-account.json");
        Files.writeString(tempFile, "{}");

        Map<String, String> apps = new HashMap<>();
        apps.put(appName, tempFile.toString());
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
        String json = "{}";
        Map<String, String> apps = new HashMap<>();
        apps.put(appName, json);
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
    void initialize_ShouldHandleException_WhenFilePathDoesNotExist() {
        // Arrange
        String appName = "invalid-app";
        String nonExistentPath = "/non/existent/path/service-account.json";

        Map<String, String> apps = new HashMap<>();
        apps.put(appName, nonExistentPath);
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
        String json1 = "{}";
        String json2 = "{}";
        Map<String, String> apps = new HashMap<>();
        apps.put("app-one", json1);
        apps.put("app-two", json2);
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
