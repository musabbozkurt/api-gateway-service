package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.filters.ApiClientFilter;
import com.mb.modulith.commerce.api.model.request.ApiOrderRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateOrderRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientOrderResponse;
import com.mb.modulith.commerce.api.model.response.ApiOrderResponse;
import com.mb.modulith.commerce.data.filters.ClientFilter;
import com.mb.modulith.commerce.data.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomOrderMapper {

    @Mapping(source = "pilotes.pilotes", target = "pilotes")
    Order map(ApiOrderRequest apiOrderRequest);

    @Mapping(source = "pilotes.pilotes", target = "pilotes")
    Order map(ApiUpdateOrderRequest apiUpdateOrderRequest);

    List<ApiOrderResponse> map(List<Order> orders);

    ApiOrderResponse map(Order order);

    default List<ApiClientOrderResponse> customMap(List<Order> orders) {
        List<ApiClientOrderResponse> apiClientOrderResponses = new ArrayList<>();
        orders.forEach(order -> apiClientOrderResponses.add(ApiClientOrderResponse.builder()
                .orderTotal(order.getOrderTotal())
                .clientId(order.getClient().getId())
                .number(order.getNumber())
                .pilotes(order.getPilotes())
                .deliveryAddressId(order.getDeliveryAddress().getId())
                .build()));
        return apiClientOrderResponses;
    }

    ClientFilter map(ApiClientFilter apiClientFilter);

}
