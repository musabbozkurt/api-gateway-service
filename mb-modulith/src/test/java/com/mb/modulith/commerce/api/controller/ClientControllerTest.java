package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiClientRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientResponse;
import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.mapping.ClientMapper;
import com.mb.modulith.commerce.service.ClientService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = ClientController.class)
@ContextConfiguration(classes = ClientController.class)
class ClientControllerTest extends BaseUnitTest {

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private ClientMapper clientMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createClient_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        Client client = getClient();
        ApiClientRequest request = new ApiClientRequest();
        when(clientMapper.map(any(ApiClientRequest.class))).thenReturn(client);
        when(clientService.createClient(client)).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(new ApiClientResponse());

        // Act
        mockMvc.perform(post("/api/client")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClientById_ShouldReturn200_WhenClientExists() throws Exception {
        // Arrange
        UUID clientId = getClient().getId();
        Client client = getClient();
        when(clientService.getClientById(clientId)).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(new ApiClientResponse());

        // Act
        mockMvc.perform(get("/api/client/{id}", clientId))
                .andExpect(status().isOk());

        // Assertions
        verify(clientService).getClientById(clientId);
    }

    @Test
    void updateClient_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        UUID clientId = getClient().getId();
        Client client = updateClient();
        ApiClientRequest request = new ApiClientRequest();
        when(clientMapper.map(any(ApiClientRequest.class))).thenReturn(client);
        when(clientService.updateClient(clientId, client)).thenReturn(client);
        when(clientMapper.map(client)).thenReturn(new ApiClientResponse());

        // Act
        mockMvc.perform(put("/api/client/{id}", clientId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteClientById_ShouldReturn200_WhenClientExists() throws Exception {
        // Arrange
        UUID clientId = getClient().getId();

        // Act
        mockMvc.perform(delete("/api/client/{id}", clientId))
                .andExpect(status().isOk());
    }
}
