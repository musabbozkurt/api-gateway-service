package com.mb.modulith.commerce.data.repository;

import com.mb.modulith.commerce.data.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndDeletedIsFalse(UUID uuid);

    Optional<Client> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
