package com.mb.modulith.school.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(nullable = false)
    private OffsetDateTime createdDateTime;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private OffsetDateTime modifiedDateTime;

    @PrePersist
    protected void onPrePersist() {
        this.createdDateTime = OffsetDateTime.now();
        this.modifiedDateTime = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.modifiedDateTime = OffsetDateTime.now();
    }
}
