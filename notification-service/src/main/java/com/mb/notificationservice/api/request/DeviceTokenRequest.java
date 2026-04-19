package com.mb.notificationservice.api.request;

import com.mb.notificationservice.enums.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRequest(@Schema(example = "fcm-device-token-abc123")
                                 @NotBlank(message = "Device token must not be blank")
                                 String token,

                                 @Schema(example = "ANDROID")
                                 @NotNull(message = "Platform must not be null")
                                 DevicePlatform platform,

                                 @Schema(example = "my-application")
                                 @NotBlank(message = "Application must not be blank")
                                 String application) {
}
