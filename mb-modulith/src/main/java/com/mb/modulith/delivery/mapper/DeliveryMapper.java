package com.mb.modulith.delivery.mapper;

import com.mb.modulith.delivery.api.request.ApiDeliveryRequest;
import com.mb.modulith.delivery.api.response.ApiDeliveryResponse;
import com.mb.modulith.delivery.data.model.Delivery;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = PersonMapper.class)
public interface DeliveryMapper {

    ApiDeliveryResponse map(Delivery delivery);

    Delivery map(ApiDeliveryRequest apiDeliveryRequest);

    List<ApiDeliveryResponse> map(List<Delivery> deliveries);
}
