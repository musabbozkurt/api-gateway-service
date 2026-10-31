package com.mb.modulith.commerce.base;

import com.mb.modulith.commerce.api.model.request.ApiClientAuthRequest;
import com.mb.modulith.commerce.api.model.response.JwtResponse;
import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.model.Order;
import com.mb.modulith.commerce.enums.UserRole;
import com.neovisionaries.i18n.CountryCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public abstract class BaseUnitTest {

    public Client getClient() {
        Client client = new Client();
        client.setId(UUID.fromString("ab9952bd-348e-4b24-8630-cc7367774807"));
        client.setPassword("test1234");
        client.setUsername("MBMUSEMENT");
        client.setFirstName("test");
        client.setLastName("test");
        client.setTelephone("1234567890");
        client.setEmail("aaa@gmail.com");
        return client;
    }

    public Client updateClient() {
        Client client = new Client();
        client.setId(UUID.fromString("ab9952bd-348e-4b24-8630-cc7367774807"));
        client.setPassword("test1234");
        client.setUsername("testtesttest");
        client.setFirstName("test1");
        client.setLastName("test1");
        client.setTelephone("1234567890");
        client.setEmail("aaa@gmail.com");
        return client;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setId(UUID.fromString("ab9952bd-348e-4b24-8630-cc7367774807"));
        address.setClient(getClient());
        address.setPostcode("postcode");
        address.setStreet("street");
        address.setCountry(CountryCode.AC);
        address.setCity("city");
        return address;
    }

    public Address createAddress() {
        Address address = new Address();
        address.setClient(getClient());
        address.setPostcode("postcode");
        address.setStreet("street");
        address.setCountry(CountryCode.AC);
        address.setCity("city");
        return address;
    }

    public ApiClientAuthRequest getApiClientAuthRequest() {
        return ApiClientAuthRequest
                .builder()
                .username("MBMUSEMENT")
                .password("test1234")
                .build();
    }

    public Order getOrder() {
        Order order = new Order();
        order.setId(UUID.fromString("ab9952bd-348e-4b24-8630-cc7367774807"));
        order.setClient(getClient());
        order.setDeliveryAddress(getAddress());
        order.setNumber("ORD-1");
        order.setPilotes(2);
        order.setOrderTotal(BigDecimal.TEN);
        order.setLocked(false);
        order.setCreatedDateTime(new Date());
        return order;
    }

    public JwtResponse getJwtResponse() {
        return new JwtResponse("token",
                "type",
                getClient().getId(),
                "username",
                "email",
                UserRole.getDefaultRoles()
                        .stream()
                        .map(Enum::name)
                        .toList());
    }

}
