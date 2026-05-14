package com.mb.inventorymanagementservice.mapper;

import com.mb.inventorymanagementservice.api.request.ApiCategoryRequest;
import com.mb.inventorymanagementservice.api.response.ApiCategoryResponse;
import com.mb.inventorymanagementservice.data.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    ApiCategoryResponse map(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    @Mapping(target = "liveInMarket", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category map(ApiCategoryRequest apiCategoryRequest);

    default Page<ApiCategoryResponse> map(Page<Category> categoryPage) {
        return categoryPage.map(this::map);
    }
}
