package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.data.repository.RoleRepository;
import com.mb.modulith.commerce.service.impl.ClientServiceImpl;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest extends BaseUnitTest {

    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        clientService = new ClientServiceImpl(clientRepository, passwordEncoder, roleRepository);
    }

    @Test
    void createClient_ShouldCreateClient_WhenThereIsNoExistingClient() {
        // Arrange
        Client client = getClient();
        when(clientRepository.save(any())).thenReturn(client);

        // Act
        clientService.createClient(client);

        // Assertions
        ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientArgumentCaptor.capture());
        Client savedClient = clientArgumentCaptor.getValue();
        assertEquals(client.getEmail(), savedClient.getEmail());
        assertEquals(client.getUsername(), savedClient.getUsername());
    }

    @Test
    void updateClient_ShouldPersistChanges_WhenClientExists() {
        // Arrange
        Client client = getClient();
        Client updated = updateClient();
        when(clientRepository.findByIdAndDeletedIsFalse(client.getId())).thenReturn(Optional.of(client));
        when(clientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        clientService.updateClient(client.getId(), updated);

        // Assertions
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertNotEquals(client.getUsername(), captor.getValue().getUsername());
    }

    @Test
    void getClientById_ShouldThrowException_WhenClientIsNotFoundForGivenParameters() {
        // Arrange
        var clientId = getClient().getId();
        when(clientRepository.findByIdAndDeletedIsFalse(clientId)).thenReturn(Optional.empty());

        // Act
        BaseException providerException = assertThrows(BaseException.class, () -> clientService.getClientById(clientId));

        // Assertions
        assertEquals("Client not found", providerException.getMessage());
    }

    @Test
    void getClientById_ShouldReturnClient_WhenClientExistsForGivenParameters() {
        // Arrange
        Client client = getClient();
        when(clientRepository.findByIdAndDeletedIsFalse(client.getId())).thenReturn(Optional.of(client));

        // Act
        Client clientById = clientService.getClientById(client.getId());

        // Assertions
        assertEquals("MBMUSEMENT", clientById.getUsername());
    }

    @Test
    void deleteClientById_ShouldThrowException_WhenClientIsNotFoundForGivenParameters() {
        // Arrange
        var clientId = getClient().getId();
        when(clientRepository.findByIdAndDeletedIsFalse(clientId)).thenReturn(Optional.empty());

        // Act
        BaseException providerException = assertThrows(BaseException.class, () -> clientService.deleteClientById(clientId));

        // Assertions
        assertEquals("Client not found", providerException.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void matchesPassword_ShouldReturnTrue_WhenEncodedHashMatches() {
        // Arrange
        String rawPassword = "BCryptPasswordEncoder";
        String encoded = new BCryptPasswordEncoder().encode(rawPassword);

        // Act
        boolean matches = new BCryptPasswordEncoder().matches(rawPassword, encoded);

        // Assertions
        assertTrue(matches);
    }
}
