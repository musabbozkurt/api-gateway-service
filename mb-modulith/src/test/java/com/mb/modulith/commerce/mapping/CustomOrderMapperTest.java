package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.filters.ApiClientFilter;
import com.mb.modulith.commerce.api.model.request.ApiOrderRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateOrderRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientOrderResponse;
import com.mb.modulith.commerce.api.model.response.ApiOrderResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.filters.ClientFilter;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.enums.PilotesEnum;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomOrderMapperTest extends BaseUnitTest {

    private final CustomOrderMapper mapper = Mappers.getMapper(CustomOrderMapper.class);

    @Test
    void map_ShouldMapOrderRequest_WhenPilotesEnumIsSet() {
        // Arrange
        ApiOrderRequest request = new ApiOrderRequest();
        request.setClientId(getClient().getId());
        request.setDeliveryAddressId(getAddress().getId());
        request.setNumber("ORD-1");
        request.setPilotes(PilotesEnum.FIVE);
        request.setOrderTotal(BigDecimal.TEN);

        // Act
        Order result = mapper.map(request);

        // Assertions
        assertEquals(5, result.getPilotes());
        assertEquals("ORD-1", result.getNumber());
    }

    @Test
    void map_ShouldMapUpdateRequest_WhenPilotesEnumIsSet() {
        // Arrange
        ApiUpdateOrderRequest request = new ApiUpdateOrderRequest();
        request.setNumber("ORD-2");
        request.setPilotes(PilotesEnum.TEN);
        request.setOrderTotal(BigDecimal.ONE);

        // Act
        Order result = mapper.map(request);

        // Assertions
        assertEquals(10, result.getPilotes());
    }

    @Test
    void map_ShouldMapOrderToResponse_WhenOrderIsValid() {
        // Arrange
        Order order = getOrder();

        // Act
        ApiOrderResponse result = mapper.map(order);

        // Assertions
        assertEquals(order.getId(), result.getId());
    }

    @Test
    void map_ShouldMapOrderList_WhenListIsNotEmpty() {
        // Arrange
        List<Order> orders = List.of(getOrder());

        // Act
        List<ApiOrderResponse> result = mapper.map(orders);

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void customMap_ShouldBuildClientOrderResponses_WhenOrdersHaveRelations() {
        // Arrange
        Order order = getOrder();

        // Act
        List<ApiClientOrderResponse> result = mapper.customMap(List.of(order));

        // Assertions
        assertEquals(1, result.size());
        ApiClientOrderResponse response = result.getFirst();
        assertEquals(order.getClient().getId(), response.getClientId());
        assertEquals(order.getDeliveryAddress().getId(), response.getDeliveryAddressId());
        assertEquals(order.getPilotes(), response.getPilotes());
    }

    @Test
    void map_ShouldMapClientFilter_WhenApiFilterIsValid() {
        // Arrange
        ApiClientFilter apiFilter = new ApiClientFilter();
        UUID clientId = getClient().getId();
        apiFilter.setClientId(clientId);
        apiFilter.setEmail("aaa@gmail.com");

        // Act
        ClientFilter result = mapper.map(apiFilter);

        // Assertions
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("aaa@gmail.com", result.getEmail());
    }
}
