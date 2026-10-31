package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.request.ApiAddressRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateAddressRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientAddressResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Address;
import com.neovisionaries.i18n.CountryCode;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomAddressMapperTest extends BaseUnitTest {

    private final CustomAddressMapper mapper = Mappers.getMapper(CustomAddressMapper.class);

    @Test
    void map_ShouldMapCreateRequestToAddress_WhenRequestIsValid() {
        // Arrange
        ApiAddressRequest request = new ApiAddressRequest();
        request.setClientId(getClient().getId());
        request.setStreet("street");
        request.setPostcode("postcode");
        request.setCity("city");
        request.setCountry(CountryCode.AC);

        // Act
        Address result = mapper.map(request);

        // Assertions
        assertEquals(request.getStreet(), result.getStreet());
        assertEquals(request.getCity(), result.getCity());
    }

    @Test
    void map_ShouldMapUpdateRequestToAddress_WhenRequestIsValid() {
        // Arrange
        ApiUpdateAddressRequest request = new ApiUpdateAddressRequest();
        request.setStreet("new-street");
        request.setPostcode("new-postcode");
        request.setCity("new-city");
        request.setCountry(CountryCode.AC);

        // Act
        Address result = mapper.map(request);

        // Assertions
        assertEquals("new-street", result.getStreet());
        assertEquals(CountryCode.AC, result.getCountry());
    }

    @Test
    void map_ShouldMapAddressToResponse_WhenClientIsSet() {
        // Arrange
        Address address = getAddress();

        // Act
        ApiClientAddressResponse result = mapper.map(address);

        // Assertions
        assertEquals(address.getClient().getId(), result.getClientId());
        assertEquals(address.getStreet(), result.getStreet());
    }

    @Test
    void map_ShouldMapAddressList_WhenListIsNotEmpty() {
        // Arrange
        List<Address> addresses = List.of(getAddress());

        // Act
        List<ApiClientAddressResponse> result = mapper.map(addresses);

        // Assertions
        assertEquals(1, result.size());
    }
}
