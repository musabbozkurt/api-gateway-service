package com.mb.modulith.commerce.messaging.queue.notifications.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Used to represent a KeyValue Pair for Template placeholders.
 */
@Getter
@Setter
@AllArgsConstructor
public class Param {
    private String key;
    private String value;
}
