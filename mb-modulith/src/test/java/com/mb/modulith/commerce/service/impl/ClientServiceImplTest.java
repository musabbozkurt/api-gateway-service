package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.data.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest extends BaseUnitTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ClientServiceImpl clientServiceImpl;

    @Test
    void createClient_ShouldPersist_WhenClientIsValid() {
        // Arrange
        Client client = getClient();
        when(clientRepository.save(client)).thenReturn(client);

        // Act
        clientServiceImpl.createClient(client);

        // Assertions
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals(client.getUsername(), captor.getValue().getUsername());
    }
}
