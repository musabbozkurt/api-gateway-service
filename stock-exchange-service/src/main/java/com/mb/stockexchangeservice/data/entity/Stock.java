package com.mb.stockexchangeservice.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stocks")
@SQLRestriction("deleted=false")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE stocks SET deleted=true WHERE id=?")
public class Stock extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal currentPrice;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "stocks")
    private List<StockExchange> stockExchanges = new ArrayList<>();
}
