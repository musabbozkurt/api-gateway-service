package com.mb.modulith.email.service.impl;

import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.data.repository.EmailRepository;
import com.mb.modulith.email.exception.EmailErrorCode;
import com.mb.modulith.email.service.EmailService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.shared.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final List<String> emailDomains = Arrays.asList("mb.com", "mb.se");
    private final EmailRepository emailRepository;

    @Override
    public Email save(Email email) {
        Optional<Email> optionalEmail = emailRepository.findByEmailAddress(email.getEmailAddress());

        if (optionalEmail.isPresent()) {
            Email optionalEmailValue = optionalEmail.get();
            return emailRepository.save(checkEmailDomain(optionalEmailValue, email.getEmailAddress(), optionalEmailValue.getEmailCount()));
        } else {
            return emailRepository.save(checkEmailDomain(email, email.getEmailAddress(), 0));
        }
    }

    private Email checkEmailDomain(Email email, String emailAddress, long emailCount) {
        if (!emailDomains.contains(EmailUtils.getEmailDomain(emailAddress)) || !EmailUtils.isValid(emailAddress)) {
            email.setEmailCount(emailCount + 1);
            email.setEmailAddress(emailAddress);
            email.setBlockedDomain(true);
            email.setDeleted(true);
            return email;
        }
        return email;
    }

    @Override
    public List<Email> findAll() {
        return emailRepository.findAll();
    }

    @Override
    public Email findById(UUID id) {
        Optional<Email> optionalEmail = emailRepository.findById(id);
        if (optionalEmail.isPresent()) {
            return optionalEmail.get();
        }
        throw new BaseException(EmailErrorCode.NOT_FOUND);
    }

    @Override
    public void deleteById(UUID id) {
        Email email = findById(id);
        email.setDeleted(true);
        save(email);
    }

    @Override
    public Email getEmailCountByEmailAddress(String emailAddress) {
        Optional<Email> optionalEmail = emailRepository.findByEmailAddress(emailAddress);
        return optionalEmail.orElse(null);
    }

    @Override
    public Email updateEmail(UUID id, Email email) {
        Email emailById = findById(id);
        return emailRepository.save(checkEmailDomain(emailById, email.getEmailAddress(), emailById.getEmailCount()));
    }
}
