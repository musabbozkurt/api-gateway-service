package com.mb.brokerageprovider.mapper;

import com.mb.brokerageprovider.api.request.ApiOrderRequest;
import com.mb.brokerageprovider.api.response.ApiOrderResponse;
import com.mb.brokerageprovider.data.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    ApiOrderResponse map(Order order);

    List<ApiOrderResponse> map(List<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "type", ignore = true)
    Order map(ApiOrderRequest apiOrderRequest);

    default Page<ApiOrderResponse> map(Page<Order> orders) {
        return orders.map(this::map);
    }
}
