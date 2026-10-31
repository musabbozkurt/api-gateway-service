package com.mb.modulith.email.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime createdDateTime;

    @Column(nullable = false)
    private boolean deleted;

    @PrePersist
    protected void onPrePersist() {
        this.id = UUID.randomUUID();
        this.createdDateTime = OffsetDateTime.now();
    }
}
