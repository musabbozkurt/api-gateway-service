package com.mb.modulith.commerce.service.impl;

import com.mb.modulith.commerce.aspect.annotation.MetricCounter;
import com.mb.modulith.commerce.data.model.Client;
import com.mb.modulith.commerce.data.model.Role;
import com.mb.modulith.commerce.data.repository.ClientRepository;
import com.mb.modulith.commerce.data.repository.RoleRepository;
import com.mb.modulith.commerce.enums.UserRole;
import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.commerce.service.ClientService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.shared.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @MetricCounter(metricType = MetricCounter.MetricType.CLIENT)
    @MetricCounter(metricType = MetricCounter.MetricType.CLIENT_NOT_FOUND, metricSubType = MetricCounter.MetricSubType.CLIENT_CREATED)
    public Client createClient(Client client) {
        if (clientRepository.existsByUsername(client.getUsername())) {
            throw new BaseException(SharedErrorCode.USERNAME_ALREADY_IN_USE);
        }

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new BaseException(SharedErrorCode.EMAIL_ALREADY_IN_USE);
        }

        //Default role for every user
        Set<Role> roles = roleRepository.findByNameInAndDeletedIsFalse(UserRole.getDefaultRoles());
        client.setRoles(roles);

        client.setPassword(passwordEncoder.encode(client.getPassword()));
        return getClient(client);
    }

    private Client getClient(Client client) {
        if (EmailUtils.isValid(client.getEmail())) {
            try {
                return clientRepository.save(client);
            } catch (Exception e) {
                throw new BaseException(SharedErrorCode.UNEXPECTED_ERROR, e.getMessage());
            }
        } else {
            throw new BaseException(SharedErrorCode.INVALID_EMAIL);
        }
    }

    @Override
    public Client updateClient(UUID id, Client client) {
        Optional<Client> optionalClient = clientRepository.findByIdAndDeletedIsFalse(id);
        if (optionalClient.isPresent()) {
            Client fetchedClient = optionalClient.get();
            fetchedClient.setFirstName(client.getFirstName());
            fetchedClient.setLastName(client.getLastName());
            fetchedClient.setTelephone(client.getTelephone());
            fetchedClient.setEmail(client.getEmail());
            fetchedClient.setPassword(passwordEncoder.encode(client.getPassword()));
            return getClient(client);
        } else {
            throw new BaseException(SharedErrorCode.CLIENT_NOT_FOUND);
        }
    }

    @Override
    public Client getClientById(UUID id) {
        Optional<Client> optionalClient = clientRepository.findByIdAndDeletedIsFalse(id);
        return optionalClient.orElseThrow(() -> new BaseException(SharedErrorCode.CLIENT_NOT_FOUND));
    }

    @Override
    public void deleteClientById(UUID id) {
        Optional<Client> optionalClient = clientRepository.findByIdAndDeletedIsFalse(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            client.setDeleted(true);
            clientRepository.save(client);
        }
        throw new BaseException(SharedErrorCode.CLIENT_NOT_FOUND);
    }
}
