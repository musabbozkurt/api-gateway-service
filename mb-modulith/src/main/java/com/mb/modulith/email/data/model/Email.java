package com.mb.modulith.email.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Table(name = "email", schema = "email")
@EntityListeners(AuditingEntityListener.class)
public class Email extends BaseEntity {

    @Column(nullable = false, unique = true)
    @Audited(withModifiedFlag = true)
    private String emailAddress;

    private long emailCount;

    private boolean blockedDomain;

}
