package com.mb.modulith.playermarket.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseDto {

    private UUID id;
    private OffsetDateTime createdDateTime;
    private boolean deleted;

}


