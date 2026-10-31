package com.mb.modulith.playermarket.data.model;

import com.mb.modulith.playermarket.enums.PositionType;
import com.neovisionaries.i18n.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "player_info", schema = "player_market")
public class PlayerInfo extends BaseEntity {
    private String name;
    private String surname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PositionType position;

    @Max(99)
    private Integer jerseyNumber;
    private String dateOfBirth;
    private String nationality;
    private String contractUntil;
    private BigDecimal marketValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode marketValueCurrency;

    @Column(nullable = false, columnDefinition = "timestamp default now()")
    private OffsetDateTime updatedDateTime = OffsetDateTime.now();

    @ToString.Exclude
    @JoinColumn(name = "team_info_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TeamInfo teamInfo;
}
