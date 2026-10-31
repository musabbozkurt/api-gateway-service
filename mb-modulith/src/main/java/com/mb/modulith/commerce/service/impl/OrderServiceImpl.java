package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.data.filters.ClientFilter;
import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.data.repository.AddressRepository;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.data.repository.OrderRepository;
import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.messaging.queue.notifications.NotificationSender;
import com.mb.modulith.commerce.messaging.queue.notifications.events.NotificationType;
import com.mb.modulith.commerce.service.OrderService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final NotificationSender notificationSender;

    @Value("${order.lock-time:5}")
    private long lockTime;

    @Override
    public Order createOrder(UUID clientId, UUID deliveryAddressId, Order order) {
        notificationSender.sendNotification(Money.of(BigDecimal.TEN, "EUR"), UUID.randomUUID(), "merchantName", NotificationType.ORDER_CREATED);
        Optional<Client> optionalClient = clientRepository.findByIdAndDeletedIsFalse(clientId);
        if (optionalClient.isPresent()) {
            order.setClient(optionalClient.get());
            setAddress(deliveryAddressId, order);
            return orderRepository.save(order);
        }
        throw new BaseException(SharedErrorCode.CLIENT_NOT_FOUND);
    }

    @Override
    public Order getOrderById(UUID id) {
        Optional<Order> optionalOrder = orderRepository.findByIdAndDeletedIsFalse(id);
        return optionalOrder.orElseThrow(() -> new BaseException(SharedErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(noRollbackFor = BaseException.class)
    public Order updateOrder(UUID id, UUID deliveryAddressId, Order order) {
        Optional<Order> optionalOrder = orderRepository.findByIdAndDeletedIsFalse(id);
        if (optionalOrder.isPresent()) {
            Order fetchedOrder = optionalOrder.get();
            isOrderLocked(fetchedOrder);
            fetchedOrder.setOrderTotal(order.getOrderTotal());
            fetchedOrder.setNumber(order.getNumber());
            fetchedOrder.setPilotes(order.getPilotes());
            setAddress(deliveryAddressId, fetchedOrder);
            return orderRepository.save(fetchedOrder);
        }
        throw new BaseException(SharedErrorCode.ORDER_NOT_FOUND);
    }

    private void isOrderLocked(Order order) {
        if (order.isLocked()) {
            throw new BaseException(SharedErrorCode.ORDER_LOCKED);
        } else {
            long createDateTime = order.getCreatedDateTime().getTime() + (lockTime * 60 * 1000L);

            if (new Date(createDateTime).before(new Date())) {
                order.setLocked(true);
                orderRepository.save(order);
                throw new BaseException(SharedErrorCode.ORDER_LOCKED);
            }
        }
    }

    @Override
    public void deleteOrderById(UUID id) {
        Optional<Order> optionalOrder = orderRepository.findByIdAndDeletedIsFalse(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setDeleted(true);
            orderRepository.save(order);
        }
        throw new BaseException(SharedErrorCode.ORDER_NOT_FOUND);
    }

    @Override
    public List<Order> getOrdersByFilter(ClientFilter clientFilter) {
        return (List<Order>) orderRepository.findAll(clientFilter.toPredicate());
    }

    private void setAddress(UUID deliveryAddressId, Order order) {
        Optional<Address> optionalAddress = addressRepository.findByIdAndDeletedIsFalse(deliveryAddressId);
        if (optionalAddress.isPresent()) {
            order.setDeliveryAddress(optionalAddress.get());
        } else {
            throw new BaseException(SharedErrorCode.ADDRESS_NOT_FOUND);
        }
    }
}
