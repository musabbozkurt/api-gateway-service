package com.mb.modulith.email.service;

import com.mb.modulith.email.data.model.Email;

import java.util.List;
import java.util.UUID;

public interface EmailService {

    Email save(Email email);

    List<Email> findAll();

    Email findById(UUID id);

    void deleteById(UUID id);

    Email getEmailCountByEmailAddress(String emailAddress);

    Email updateEmail(UUID id, Email email);
}
