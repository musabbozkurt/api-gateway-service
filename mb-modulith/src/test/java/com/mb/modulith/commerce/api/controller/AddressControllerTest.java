package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiAddressRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientAddressResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.mapping.CustomAddressMapper;
import com.mb.modulith.commerce.service.AddressService;
import com.mb.modulith.support.CommerceWebMvcTestSecurity;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = AddressController.class)
@ContextConfiguration(classes = AddressController.class)
class AddressControllerTest extends BaseUnitTest {

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private CustomAddressMapper customMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAddress_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        UUID clientId = getClient().getId();
        Address address = getAddress();
        ApiAddressRequest request = new ApiAddressRequest();
        request.setClientId(clientId);
        when(customMapper.map(any(ApiAddressRequest.class))).thenReturn(address);
        when(addressService.createAddress(clientId, address)).thenReturn(address);
        when(customMapper.map(address)).thenReturn(new ApiClientAddressResponse());

        // Act
        mockMvc.perform(post("/api/client/address")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAddressById_ShouldReturn200_WhenAddressExists() throws Exception {
        // Arrange
        UUID id = getAddress().getId();
        Address address = getAddress();
        when(addressService.getAddressById(id)).thenReturn(address);
        when(customMapper.map(address)).thenReturn(new ApiClientAddressResponse());

        // Act
        mockMvc.perform(get("/api/client/address/{id}", id))
                .andExpect(status().isOk());

        // Assertions
        verify(addressService).getAddressById(id);
    }
}
