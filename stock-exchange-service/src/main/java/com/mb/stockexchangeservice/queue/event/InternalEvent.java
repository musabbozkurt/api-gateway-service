package com.mb.stockexchangeservice.queue.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InternalEvent extends BaseEventDto implements Event {

    private UUID randomId;

    @Override
    public EventType getEventType() {
        return StockExchangeServiceEventType.INTERNAL_EVENT;
    }
}
