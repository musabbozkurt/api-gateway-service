package com.mb.modulith.email.data.repository;

import com.mb.modulith.email.data.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<Email, UUID> {

    Optional<Email> findByEmailAddress(String emailAddress);
}
