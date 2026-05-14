package com.mb.stockexchangeservice.queue.event;

import com.mb.stockexchangeservice.data.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEventDto implements Event {

    private User user;

    @Override
    public EventType getEventType() {
        return StockExchangeServiceEventType.USER_CREATED_EVENT;
    }
}
