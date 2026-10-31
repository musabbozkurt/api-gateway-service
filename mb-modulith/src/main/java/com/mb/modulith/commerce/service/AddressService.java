package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.data.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    Address createAddress(UUID clientId, Address address);

    Address getAddressById(UUID id);

    Address updateAddress(UUID id, Address address);

    void deleteAddressById(UUID id);

    List<Address> getAddresssByClientId(UUID clientId);
}
