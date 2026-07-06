package com.mb.notificationservice.config.type_safe;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table("person")
public record Person(@Id Long id,
                     String firstName,
                     String lastName,
                     @Embedded.Nullable Address address) {
}
