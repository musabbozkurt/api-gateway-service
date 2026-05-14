package com.mb.inventorymanagementservice.mapper;

import com.mb.inventorymanagementservice.api.request.ApiProductRequest;
import com.mb.inventorymanagementservice.api.response.ApiProductResponse;
import com.mb.inventorymanagementservice.data.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ApiProductResponse map(Product product);

    List<ApiProductResponse> map(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product map(ApiProductRequest apiProductRequest);

    default Page<ApiProductResponse> map(Page<Product> productPage) {
        return productPage.map(this::map);
    }
}
