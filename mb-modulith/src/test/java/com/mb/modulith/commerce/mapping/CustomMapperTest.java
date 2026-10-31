package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.request.ApiOrderRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientOrderResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.enums.PilotesEnum;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomMapperTest extends BaseUnitTest {

    private final CustomMapper mapper = Mappers.getMapper(CustomMapper.class);

    @Test
    void map_ShouldResolvePilotesEnum_WhenIdIsKnown() {
        // Act
        PilotesEnum result = mapper.map(5);

        // Assertions
        assertEquals(PilotesEnum.FIVE, result);
    }

    @Test
    void map_ShouldReturnNull_WhenPilotesIdIsUnknown() {
        // Act
        PilotesEnum result = mapper.map(99);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldMapOrderRequest_WhenRequestIsValid() {
        // Arrange
        ApiOrderRequest request = new ApiOrderRequest();
        request.setPilotes(PilotesEnum.FIFTEEN);
        request.setNumber("ORD-99");

        // Act
        Order result = mapper.map(request);

        // Assertions
        assertEquals(15, result.getPilotes());
        assertEquals("ORD-99", result.getNumber());
    }

    @Test
    void customMap_ShouldMapOrdersToClientOrderResponses_WhenOrdersAreValid() {
        // Arrange
        Order order = getOrder();

        // Act
        List<ApiClientOrderResponse> result = mapper.customMap(List.of(order));

        // Assertions
        assertEquals(1, result.size());
        assertEquals(order.getNumber(), result.getFirst().getNumber());
    }
}
