package com.mb.modulith.email.service.impl;

import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.data.repository.EmailRepository;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void findAll_ShouldReturnRepositoryResults_WhenEmailsExist() {
        // Arrange
        when(emailRepository.findAll()).thenReturn(List.of(new Email()));

        // Act
        List<Email> result = emailService.findAll();

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void findById_ShouldReturnEmail_WhenPresent() {
        // Arrange
        UUID id = UUID.randomUUID();
        Email email = new Email();
        when(emailRepository.findById(id)).thenReturn(Optional.of(email));

        // Act
        Email result = emailService.findById(id);

        // Assertions
        assertEquals(email, result);
    }

    @Test
    void findById_ShouldThrow_WhenMissing() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(emailRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> emailService.findById(id));
    }

    @Test
    void save_ShouldPersistNewValidEmail_WhenAddressIsUnique() {
        // Arrange
        Email email = new Email();
        email.setEmailAddress("user@mb.com");
        when(emailRepository.findByEmailAddress(email.getEmailAddress())).thenReturn(Optional.empty());
        when(emailRepository.save(any(Email.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Email result = emailService.save(email);

        // Assertions
        verify(emailRepository).save(any(Email.class));
        assertEquals("user@mb.com", result.getEmailAddress());
    }

    @Test
    void deleteById_ShouldMarkDeleted_WhenEmailExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Email email = new Email();
        email.setEmailAddress("user@mb.com");
        when(emailRepository.findById(id)).thenReturn(Optional.of(email));
        when(emailRepository.findByEmailAddress(email.getEmailAddress())).thenReturn(Optional.of(email));
        when(emailRepository.save(any(Email.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        emailService.deleteById(id);

        // Assertions
        verify(emailRepository).save(any(Email.class));
    }
}
