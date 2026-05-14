package com.mb.stockexchangeservice.queue.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEventDto {

    @Builder.Default
    private UUID id = UUID.randomUUID();
}
