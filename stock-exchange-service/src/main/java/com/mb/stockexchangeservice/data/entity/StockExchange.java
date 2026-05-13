package com.mb.stockexchangeservice.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_exchanges")
@SQLRestriction("deleted=false")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE stock_exchanges SET deleted=true WHERE id=?")
public class StockExchange extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean liveInMarket;

    @Version
    private int version;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "stock_exchanges_stocks",
            joinColumns = @JoinColumn(name = "stock_exchange_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_id")
    )
    private List<Stock> stocks = new ArrayList<>();

    public void addStock(Stock stock) {
        stocks.add(stock);
        stock.getStockExchanges().add(this);
    }

    public void removeStock(Stock stock) {
        stocks.remove(stock);
        stock.getStockExchanges().remove(this);
    }
}
