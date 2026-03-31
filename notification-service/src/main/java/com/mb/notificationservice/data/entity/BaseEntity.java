package com.mb.notificationservice.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final String DEFAULT_CREATED_BY = "System";

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.lastModifiedDate = now;
        if (this.createdBy == null || this.createdBy.isBlank()) {
            this.createdBy = DEFAULT_CREATED_BY;
        }
        if (this.lastModifiedBy == null || this.lastModifiedBy.isBlank()) {
            this.lastModifiedBy = this.createdBy;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
        if (this.lastModifiedBy == null || this.lastModifiedBy.isBlank()) {
            this.lastModifiedBy = DEFAULT_CREATED_BY;
        }
    }
}
