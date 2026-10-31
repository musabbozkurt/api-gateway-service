package com.mb.modulith.email.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ApiEmailResponse {

    private UUID id;
    private OffsetDateTime createdDateTime;
    private boolean deleted;
    private String emailAddress;
    private long emailCount;
    private boolean blockedDomain;

}
