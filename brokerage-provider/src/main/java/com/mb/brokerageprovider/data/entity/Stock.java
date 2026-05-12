package com.mb.brokerageprovider.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

    private String productCode;
    private Long quantity;
}
