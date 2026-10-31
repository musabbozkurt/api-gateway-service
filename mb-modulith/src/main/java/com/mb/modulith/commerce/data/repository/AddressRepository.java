package com.mb.modulith.commerce.data.repository;

import com.mb.modulith.commerce.data.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    Optional<Address> findByIdAndDeletedIsFalse(UUID id);

    List<Address> findByClient_IdAndDeletedIsFalse(UUID clientId);
}
