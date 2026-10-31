package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.request.ApiClientRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Client;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientMapperTest extends BaseUnitTest {

    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void map_ShouldMapRequestToClient_WhenRequestIsValid() {
        // Arrange
        ApiClientRequest request = new ApiClientRequest();
        request.setFirstName("test");
        request.setLastName("test");
        request.setTelephone("1234567890");
        request.setUsername("MBMUSEMENT");
        request.setPassword("secret");
        request.setEmail("aaa@gmail.com");

        // Act
        Client result = clientMapper.map(request);

        // Assertions
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getUsername(), result.getUsername());
    }

    @Test
    void map_ShouldMapClientToResponse_WhenClientIsValid() {
        // Arrange
        Client client = getClient();

        // Act
        ApiClientResponse result = clientMapper.map(client);

        // Assertions
        assertEquals(client.getId(), result.getId());
        assertEquals(client.getUsername(), result.getUsername());
    }

    @Test
    void map_ShouldMapClientList_WhenListIsNotEmpty() {
        // Arrange
        List<Client> clients = List.of(getClient());

        // Act
        List<ApiClientResponse> result = clientMapper.map(clients);

        // Assertions
        assertEquals(1, result.size());
        assertEquals(clients.getFirst().getUsername(), result.getFirst().getUsername());
    }
}
