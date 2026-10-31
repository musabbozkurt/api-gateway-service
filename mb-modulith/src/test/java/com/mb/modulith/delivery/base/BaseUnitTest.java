package com.mb.modulith.delivery.base;

import com.mb.modulith.delivery.api.request.ApiDeliveryRequest;
import com.mb.modulith.delivery.api.request.ApiPersonRequest;
import com.mb.modulith.delivery.data.model.Delivery;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.enums.ParticipantType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public abstract class BaseUnitTest {

    public Delivery getDelivery() {
        final Delivery delivery = new Delivery();
        delivery.setId(0L);
        delivery.setStartTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        delivery.setEndTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        delivery.setDistance(0L);
        delivery.setPrice(0L);
        delivery.setComission(0L);
        delivery.setDeliveryMan(getDeliveryMan());
        delivery.setCustomer(getCustomer());
        return delivery;
    }

    public Person getDeliveryMan() {
        final Person deliveryMan = new Person();
        deliveryMan.setId(0L);
        deliveryMan.setName("name");
        deliveryMan.setEmail("email");
        deliveryMan.setRegistrationNumber("registrationNumber");
        deliveryMan.setType(ParticipantType.DELIVERY_MAN);
        return deliveryMan;
    }

    public Person getCustomer() {
        final Person customer = new Person();
        customer.setId(0L);
        customer.setName("name");
        customer.setEmail("email");
        customer.setRegistrationNumber("registrationNumber");
        customer.setType(ParticipantType.CUSTOMER);
        return customer;
    }

    public Delivery getDelivery1() {
        final Delivery delivery1 = new Delivery();
        delivery1.setId(0L);
        delivery1.setStartTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        delivery1.setEndTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        delivery1.setDistance(0L);
        delivery1.setPrice(0L);
        delivery1.setComission(0L);
        delivery1.setDeliveryMan(getDeliveryMan2());
        delivery1.setCustomer(getCustomer2());
        return delivery1;
    }

    public Person getDeliveryMan2() {
        final Person deliveryMan2 = new Person();
        deliveryMan2.setId(0L);
        deliveryMan2.setName("name");
        deliveryMan2.setEmail("email");
        deliveryMan2.setRegistrationNumber("registrationNumber");
        deliveryMan2.setType(ParticipantType.DELIVERY_MAN);
        return deliveryMan2;
    }

    public Person getCustomer2() {
        final Person customer2 = new Person();
        customer2.setId(0L);
        customer2.setName("name");
        customer2.setEmail("email");
        customer2.setRegistrationNumber("registrationNumber");
        customer2.setType(ParticipantType.CUSTOMER);
        return customer2;
    }

    public ApiPersonRequest getApiPersonRequest() {
        final ApiPersonRequest apiPersonRequest = new ApiPersonRequest();
        apiPersonRequest.setId(0L);
        apiPersonRequest.setName("name");
        apiPersonRequest.setEmail("email");
        apiPersonRequest.setRegistrationNumber("registrationNumber");
        apiPersonRequest.setType(ParticipantType.CUSTOMER);
        return apiPersonRequest;
    }

    public List<Person> getAllPersons() {
        return Arrays.asList(getCustomer(), getDeliveryMan());
    }

    public List<Delivery> getDeliveryList() {
        return Arrays.asList(getDelivery(), getDelivery1());
    }

    public ApiDeliveryRequest getApiDeliveryRequest() {
        final ApiDeliveryRequest apiDeliveryRequest = new ApiDeliveryRequest();
        apiDeliveryRequest.setId(0L);
        apiDeliveryRequest.setStartTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        apiDeliveryRequest.setEndTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        apiDeliveryRequest.setDistance(0L);
        apiDeliveryRequest.setPrice(0L);
        apiDeliveryRequest.setComission(0L);
        apiDeliveryRequest.setDeliveryMan(getDeliveryMan2());
        apiDeliveryRequest.setCustomer(getCustomer2());
        return apiDeliveryRequest;
    }
}
