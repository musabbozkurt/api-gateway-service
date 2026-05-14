package com.mb.inventorymanagementservice.data.repository;

import com.mb.inventorymanagementservice.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameIgnoreCase(String name);

    @Query(value = "SELECT p.quantity FROM Product p WHERE p.productCode = :productCode")
    Integer findQuantityByProductCode(String productCode);
}
