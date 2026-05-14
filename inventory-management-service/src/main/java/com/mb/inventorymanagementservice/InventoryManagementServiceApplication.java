package com.mb.inventorymanagementservice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class InventoryManagementServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(InventoryManagementServiceApplication.class, args);
    }
}
