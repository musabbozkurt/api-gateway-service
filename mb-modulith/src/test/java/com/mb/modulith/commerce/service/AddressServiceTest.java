package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.data.repository.AddressRepository;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.service.impl.AddressServiceImpl;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest extends BaseUnitTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private List<Address> addressList;
    private Address address;
    private Address address1;

    @BeforeEach
    void setUp() {
        addressList = new ArrayList<>();
        address = createAddress();
        address1 = createAddress();
        addressList.add(address);
        addressList.add(address1);
    }

    @AfterEach
    void tearDown() {
        address = address1 = null;
        addressList = null;
    }

    @Test
    void createAddress_ShouldThrowException_WhenClientNotFound() {
        // Arrange
        UUID clientId = getClient().getId();

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> addressService.createAddress(clientId, address));

        // Assertions
        assertEquals("Client not found", exception.getMessage());
    }

    @Test
    void getAddressById_ShouldReturnAddress_WhenAddressExists() {
        // Arrange
        UUID addressId = getAddress().getId();
        Address stored = getAddress();
        when(addressRepository.findByIdAndDeletedIsFalse(addressId)).thenReturn(Optional.of(stored));

        // Act
        Address result = addressService.getAddressById(addressId);

        // Assertions
        assertEquals(stored.getId(), result.getId());
    }
}
