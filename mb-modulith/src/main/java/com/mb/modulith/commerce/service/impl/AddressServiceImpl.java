package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.repository.AddressRepository;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.service.AddressService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;

    @Override
    public Address createAddress(UUID clientId, Address address) {
        Optional<Client> optionalClient = clientRepository.findByIdAndDeletedIsFalse(clientId);
        if (optionalClient.isPresent()) {
            address.setClient(optionalClient.get());
            return addressRepository.save(address);
        }
        throw new BaseException(SharedErrorCode.CLIENT_NOT_FOUND);
    }

    @Override
    public Address getAddressById(UUID id) {
        Optional<Address> optionalAddress = addressRepository.findByIdAndDeletedIsFalse(id);
        return optionalAddress.orElseThrow(() -> new BaseException(SharedErrorCode.ADDRESS_NOT_FOUND));
    }

    @Override
    public Address updateAddress(UUID id, Address address) {
        Optional<Address> optionalAddress = addressRepository.findByIdAndDeletedIsFalse(id);
        if (optionalAddress.isPresent()) {
            Address fetchedAddress = optionalAddress.get();
            fetchedAddress.setCity(address.getCity());
            fetchedAddress.setCountry(address.getCountry());
            fetchedAddress.setPostcode(address.getPostcode());
            fetchedAddress.setStreet(address.getStreet());
            return addressRepository.save(fetchedAddress);
        }
        throw new BaseException(SharedErrorCode.ADDRESS_NOT_FOUND);
    }

    @Override
    public void deleteAddressById(UUID id) {
        Optional<Address> optionalAddress = addressRepository.findByIdAndDeletedIsFalse(id);
        if (optionalAddress.isPresent()) {
            Address address = optionalAddress.get();
            address.setDeleted(true);
            addressRepository.save(address);
        }
        throw new BaseException(SharedErrorCode.ADDRESS_NOT_FOUND);
    }

    @Override
    public List<Address> getAddresssByClientId(UUID clientId) {
        return addressRepository.findByClient_IdAndDeletedIsFalse(clientId);
    }
}
