package com.mb.modulith.commerce.service;

import com.mb.modulith.commerce.data.model.Client;

import java.util.UUID;

public interface ClientService {
    Client createClient(Client client);

    Client getClientById(UUID id);

    void deleteClientById(UUID id);

    Client updateClient(UUID id, Client client);
}
