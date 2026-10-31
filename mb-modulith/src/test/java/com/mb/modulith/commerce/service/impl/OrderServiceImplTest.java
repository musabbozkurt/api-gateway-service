package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.data.repository.AddressRepository;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.data.repository.OrderRepository;
import com.mb.modulith.commerce.messaging.queue.notifications.NotificationSender;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest extends BaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // Arrange
        ReflectionTestUtils.setField(orderService, "lockTime", 60L);
    }

    @Test
    void createOrder_ShouldPersist_WhenClientAndAddressExist() {
        // Arrange
        Client client = getClient();
        Order order = getOrder();
        UUID clientId = client.getId();
        UUID addressId = getAddress().getId();
        when(clientRepository.findByIdAndDeletedIsFalse(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findByIdAndDeletedIsFalse(addressId)).thenReturn(Optional.of(getAddress()));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Order result = orderService.createOrder(clientId, addressId, order);

        // Assertions
        assertEquals(client, result.getClient());
        verify(notificationSender).sendNotification(any(), any(), any(), any());
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_ShouldThrow_WhenClientMissing() {
        // Arrange
        UUID clientId = getClient().getId();
        UUID addressId = getAddress().getId();
        Order order = getOrder();
        when(clientRepository.findByIdAndDeletedIsFalse(clientId)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> orderService.createOrder(clientId, addressId, order));
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenFound() {
        // Arrange
        Order order = getOrder();
        when(orderRepository.findByIdAndDeletedIsFalse(order.getId())).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderById(order.getId());

        // Assertions
        assertEquals(order.getId(), result.getId());
    }

    @Test
    void getOrderById_ShouldThrow_WhenNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(orderRepository.findByIdAndDeletedIsFalse(id)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> orderService.getOrderById(id));
    }

    @Test
    void updateOrder_ShouldPersist_WhenOrderIsUnlocked() {
        // Arrange
        Order stored = getOrder();
        UUID orderId = stored.getId();
        UUID addressId = getAddress().getId();
        Order patch = getOrder();
        when(orderRepository.findByIdAndDeletedIsFalse(orderId)).thenReturn(Optional.of(stored));
        when(addressRepository.findByIdAndDeletedIsFalse(addressId)).thenReturn(Optional.of(getAddress()));
        when(orderRepository.save(stored)).thenReturn(stored);

        // Act
        Order result = orderService.updateOrder(orderId, addressId, patch);

        // Assertions
        assertEquals(stored, result);
        verify(orderRepository).save(stored);
    }

    @Test
    void updateOrder_ShouldThrow_WhenOrderIsLocked() {
        // Arrange
        Order stored = getOrder();
        stored.setLocked(true);
        UUID orderId = stored.getId();
        UUID addressId = getAddress().getId();
        Order order = getOrder();
        when(orderRepository.findByIdAndDeletedIsFalse(orderId)).thenReturn(Optional.of(stored));

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> orderService.updateOrder(orderId, addressId, order));
    }
}
