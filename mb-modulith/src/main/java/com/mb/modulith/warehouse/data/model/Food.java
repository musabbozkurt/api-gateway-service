package com.mb.modulith.warehouse.data.model;

import com.mb.modulith.warehouse.utils.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "food", schema = "warehouse")
@SQLDelete(sql = "UPDATE warehouse.food SET deleted = true WHERE id=?")
@NamedQuery(name = Constants.FOOD_FIND_ALL, query = "select food from Food food")
@NamedQuery(name = Constants.FOOD_FIND_BY_ID, query = "select food from Food food where food.id = :id")
@NamedQuery(name = Constants.FOOD_FIND_BY_NAME, query = "select food from Food food where food.name = :name")
public class Food {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_date_time", nullable = false)
    private OffsetDateTime createdDateTime;

    @Column(name = "modified_date_time", nullable = false)
    private OffsetDateTime modifiedDateTime;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "name")
    private String name;

    @Column(name = "weight")
    private BigDecimal weight;

    public Food(int id, String name, BigDecimal weight) {
        this.id = id;
        this.name = name;
        this.weight = weight;
    }

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
