package com.mb.inventorymanagementservice.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    private String username;
    private String email;
    private String mobileNumber;
    private Locale language;
    private String ipAddress;
    private String userAgent;
    private boolean admin;
    private ZoneOffset preferredZoneOffset;
}
