package com.mb.modulith.delivery.mapper;

import com.mb.modulith.delivery.api.request.ApiDeliveryRequest;
import com.mb.modulith.delivery.api.response.ApiDeliveryResponse;
import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Delivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeliveryMapperTest extends BaseUnitTest {

    private final PersonMapper personMapper = new PersonMapperImpl();
    private DeliveryMapper deliveryMapper;

    @BeforeEach
    void setUp() {
        deliveryMapper = new DeliveryMapperImpl();
        ReflectionTestUtils.setField(deliveryMapper, "personMapper", personMapper);
    }

    @Test
    void map_ShouldMapDeliveryToApiDeliveryResponse_WhenDeliveryIsValid() {
        // Arrange
        Delivery delivery = getDelivery();

        // Act
        ApiDeliveryResponse result = deliveryMapper.map(delivery);

        // Assertions
        assertEquals(delivery.getId(), result.getId());
        assertEquals(delivery.getCustomer().getName(), result.getCustomer().getName());
        assertEquals(delivery.getEndTime(), result.getEndTime());
    }

    @Test
    void map_ShouldMapApiDeliveryRequestToDelivery_WhenRequestIsValid() {
        // Arrange
        ApiDeliveryRequest apiDeliveryRequest = getApiDeliveryRequest();

        // Act
        Delivery result = deliveryMapper.map(apiDeliveryRequest);

        // Assertions
        assertEquals(apiDeliveryRequest.getDeliveryManId(), result.getDeliveryManId());
        assertEquals(apiDeliveryRequest.getComission(), result.getComission());
        assertEquals(apiDeliveryRequest.getDistance(), result.getDistance());
    }

    @Test
    void map_ShouldMapAllDeliveries_WhenListIsProvided() {
        // Arrange
        List<Delivery> deliveryList = getDeliveryList();

        // Act
        List<ApiDeliveryResponse> result = deliveryMapper.map(deliveryList);

        // Assertions
        assertEquals(deliveryList.size(), result.size());
    }

    @Test
    void map_ShouldReturnNull_WhenDeliveryIsNull() {
        // Act
        ApiDeliveryResponse result = deliveryMapper.map((Delivery) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldReturnNull_WhenApiDeliveryRequestIsNull() {
        // Act
        Delivery result = deliveryMapper.map((ApiDeliveryRequest) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldMapNestedParticipants_WhenDeliveryHasRelations() {
        // Arrange
        Delivery delivery = getDelivery();
        delivery.setPrice(99L);

        // Act
        ApiDeliveryResponse result = deliveryMapper.map(delivery);

        // Assertions
        assertEquals(delivery.getPrice(), result.getPrice());
        assertEquals(delivery.getDeliveryMan().getEmail(), result.getDeliveryMan().getEmail());
        assertEquals(delivery.getCustomer().getType(), result.getCustomer().getType());
    }
}
