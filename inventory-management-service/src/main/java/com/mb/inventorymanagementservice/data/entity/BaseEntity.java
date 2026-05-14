package com.mb.inventorymanagementservice.data.entity;

import com.mb.inventorymanagementservice.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@MappedSuperclass
@Audited(withModifiedFlag = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Constants.DEFAULT_ID_GENERATOR_NAME)
    @SequenceGenerator(name = Constants.DEFAULT_ID_GENERATOR_NAME, sequenceName = "default_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private OffsetDateTime createdDateTime;

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
