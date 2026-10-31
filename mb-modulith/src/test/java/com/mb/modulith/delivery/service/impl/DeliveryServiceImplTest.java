package com.mb.modulith.delivery.service.impl;

import com.mb.modulith.delivery.api.response.ApiTopCourierResponse;
import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.data.repository.DeliveryRepository;
import com.mb.modulith.delivery.data.repository.PersonRepository;
import com.mb.modulith.delivery.enums.ParticipantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest extends BaseUnitTest {

    @Mock
    private DeliveryRepository mockDeliveryRepository;

    @Mock
    private PersonRepository mockPersonRepository;

    private DeliveryServiceImpl deliveryServiceImplUnderTest;

    @BeforeEach
    void setUp() {
        deliveryServiceImplUnderTest = new DeliveryServiceImpl(mockDeliveryRepository, mockPersonRepository);
    }

    @Test
    void save_ShouldReturnDelivery_WhenValidationPassesAndRepositorySaves() {
        // Arrange
        Delivery delivery = getDelivery();
        when(mockDeliveryRepository.findOverlappingForDeliveryMan(any(), any(), any(), any())).thenReturn(List.of());
        when(mockDeliveryRepository.save(delivery)).thenReturn(delivery);

        // Act
        Delivery result = deliveryServiceImplUnderTest.save(delivery);

        // Assertions
        assertEquals(delivery, result);
    }

    @Test
    void findById_ShouldReturnDelivery_WhenRepositoryFindsEntity() {
        // Arrange
        Delivery delivery = getDelivery1();
        when(mockDeliveryRepository.findById(0L)).thenReturn(Optional.of(delivery));

        // Act
        Delivery result = deliveryServiceImplUnderTest.findById(0L);

        // Assertions
        assertEquals(delivery, result);
    }

    @Test
    void findById_ShouldReturnNull_WhenRepositoryReturnsEmpty() {
        // Arrange
        when(mockDeliveryRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Delivery result = deliveryServiceImplUnderTest.findById(0L);

        // Assertions
        assertNull(result);
    }

    @Test
    void save_ShouldApplyCommission_WhenCommissionIsMissing() {
        // Arrange
        Delivery delivery = getDelivery();
        delivery.setComission(null);
        delivery.setPrice(100L);
        delivery.setDistance(10L);
        when(mockDeliveryRepository.findOverlappingForDeliveryMan(any(), any(), any(), any())).thenReturn(List.of());
        when(mockDeliveryRepository.save(delivery)).thenReturn(delivery);

        // Act
        Delivery result = deliveryServiceImplUnderTest.save(delivery);

        // Assertions
        assertEquals(10L, result.getComission());
    }

    @Test
    void save_ShouldThrow_WhenDeliveryManHasOverlappingDelivery() {
        // Arrange
        Delivery delivery = getDelivery();
        when(mockDeliveryRepository.findOverlappingForDeliveryMan(delivery.getDeliveryMan().getId(), delivery.getStartTime(), delivery.getEndTime(), delivery.getId())).thenReturn(List.of(getDelivery1()));

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> deliveryServiceImplUnderTest.save(delivery));

        // Assertions
        assertEquals(IllegalArgumentException.class, exception.getClass());
    }

    @Test
    void save_ShouldThrow_WhenDeliveryManTypeIsNotDeliveryMan() {
        // Arrange
        Delivery delivery = getDelivery();
        Person customerAsCourier = getCustomer();
        delivery.setDeliveryMan(customerAsCourier);

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> deliveryServiceImplUnderTest.save(delivery));

        // Assertions
        assertEquals(IllegalArgumentException.class, exception.getClass());
    }

    @Test
    void save_ShouldResolveParticipants_WhenOnlyIdsAreProvided() {
        // Arrange
        Delivery delivery = new Delivery();
        delivery.setStartTime(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
        delivery.setEndTime(LocalDateTime.of(2020, 1, 1, 1, 0).toInstant(ZoneOffset.UTC));
        delivery.setDeliveryManId(1L);
        delivery.setCustomerId(2L);
        delivery.setPrice(50L);
        delivery.setDistance(4L);
        Person deliveryMan = getDeliveryMan();
        deliveryMan.setId(1L);
        Person customer = getCustomer();
        customer.setId(2L);
        when(mockPersonRepository.findById(1L)).thenReturn(Optional.of(deliveryMan));
        when(mockPersonRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(mockDeliveryRepository.findOverlappingForDeliveryMan(any(), any(), any(), any())).thenReturn(List.of());
        when(mockDeliveryRepository.save(delivery)).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Delivery result = deliveryServiceImplUnderTest.save(delivery);

        // Assertions
        assertEquals(ParticipantType.DELIVERY_MAN, result.getDeliveryMan().getType());
        assertEquals(ParticipantType.CUSTOMER, result.getCustomer().getType());
        verify(mockDeliveryRepository).save(delivery);
    }

    @Test
    void findTopCouriersByMaxOrderPrice_ShouldRankByHighestMaxPrice_WhenDeliveriesFallInInterval() {
        // Arrange
        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2020-01-02T00:00:00Z");
        Person courierOne = getDeliveryMan();
        courierOne.setId(1L);
        Person courierTwo = getDeliveryMan2();
        courierTwo.setId(2L);

        Delivery lowPriceDelivery = getDelivery();
        lowPriceDelivery.setDeliveryMan(courierOne);
        lowPriceDelivery.setStartTime(from);
        lowPriceDelivery.setPrice(50L);
        lowPriceDelivery.setComission(5L);

        Delivery highPriceDelivery = getDelivery1();
        highPriceDelivery.setDeliveryMan(courierTwo);
        highPriceDelivery.setStartTime(from);
        highPriceDelivery.setPrice(200L);
        highPriceDelivery.setComission(20L);

        Delivery secondDeliveryForCourierOne = getDelivery();
        secondDeliveryForCourierOne.setDeliveryMan(courierOne);
        secondDeliveryForCourierOne.setStartTime(from);
        secondDeliveryForCourierOne.setPrice(80L);
        secondDeliveryForCourierOne.setComission(8L);

        when(mockDeliveryRepository.findAll()).thenReturn(List.of(lowPriceDelivery, highPriceDelivery, secondDeliveryForCourierOne));

        // Act
        List<ApiTopCourierResponse> result = deliveryServiceImplUnderTest.findTopCouriersByMaxOrderPrice(from, to, 2);

        // Assertions
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getDeliveryManId());
        assertEquals(200L, result.get(0).getMaxOrderPrice());
        assertEquals(1L, result.get(1).getDeliveryManId());
        assertEquals(80L, result.get(1).getMaxOrderPrice());
    }

    @Test
    void findTopCouriersByMaxOrderPrice_ShouldThrow_WhenIntervalIsInvalid() {
        // Arrange
        Instant from = Instant.parse("2020-01-02T00:00:00Z");
        Instant to = Instant.parse("2020-01-01T00:00:00Z");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> deliveryServiceImplUnderTest.findTopCouriersByMaxOrderPrice(from, to, 3));

        // Assertions
        assertEquals(IllegalArgumentException.class, exception.getClass());
    }
}
