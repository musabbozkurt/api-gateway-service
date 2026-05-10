package com.mb.kafkadebeziumservice.queue.event.producer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class Customer {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
