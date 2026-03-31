package com.mb.notificationservice.service;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.data.repository.NotificationTemplateRepository;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.mapper.NotificationTemplateMapper;
import com.mb.notificationservice.service.impl.NotificationTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @InjectMocks
    private NotificationTemplateServiceImpl notificationTemplateService;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Spy
    @SuppressWarnings("unused")
    private NotificationTemplateMapper notificationTemplateMapper;

    private NotificationTemplateRequest validRequest;
    private NotificationTemplate notificationTemplate;

    @BeforeEach
    void setUp() {
        validRequest = new NotificationTemplateRequest();
        validRequest.setChannel(NotificationChannel.EMAIL);
        validRequest.setCode("WELCOME_EMAIL");
        validRequest.setName("Welcome Email");
        validRequest.setSubject("Welcome {{name}}!");
        validRequest.setBody("Hello {{name}}, welcome to our platform!");
        validRequest.setDescription("Welcome email template");
        validRequest.setActive(true);

        notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId(1L);
        notificationTemplate.setChannel(NotificationChannel.EMAIL);
        notificationTemplate.setCode("WELCOME_EMAIL");
        notificationTemplate.setName("Welcome Email");
        notificationTemplate.setSubject("Welcome {{name}}!");
        notificationTemplate.setBody("Hello {{name}}, welcome to our platform!");
        notificationTemplate.setDescription("Welcome email template");
        notificationTemplate.setActive(true);
    }

    @Test
    void create_ShouldCreateTemplate_WhenCodeAndChannelDoNotExist() {
        when(notificationTemplateRepository.existsByCodeAndChannel(anyString(), any(NotificationChannel.class))).thenReturn(false);
        when(notificationTemplateRepository.save(any(NotificationTemplate.class))).thenReturn(notificationTemplate);

        NotificationTemplateResponse response = notificationTemplateService.create(validRequest);

        assertNotNull(response);
        assertEquals("WELCOME_EMAIL", response.getCode());
        verify(notificationTemplateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    void create_ShouldThrowException_WhenCodeAndChannelAlreadyExist() {
        when(notificationTemplateRepository.existsByCodeAndChannel(anyString(), any(NotificationChannel.class))).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> notificationTemplateService.create(validRequest));
        assertEquals(NotificationErrorCode.NOTIFICATION_TEMPLATE_CODE_EXISTS, exception.getErrorCode());
        verify(notificationTemplateRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateTemplate_WhenTemplateExists() {
        when(notificationTemplateRepository.findById(1L)).thenReturn(Optional.of(notificationTemplate));
        when(notificationTemplateRepository.save(any(NotificationTemplate.class))).thenReturn(notificationTemplate);

        NotificationTemplateResponse response = notificationTemplateService.update(1L, validRequest);

        assertNotNull(response);
        verify(notificationTemplateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    void update_ShouldThrowException_WhenTemplateNotFound() {
        when(notificationTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> notificationTemplateService.update(1L, validRequest));
        assertEquals(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getById_ShouldReturnTemplate_WhenTemplateExists() {
        when(notificationTemplateRepository.findById(1L)).thenReturn(Optional.of(notificationTemplate));

        NotificationTemplateResponse response = notificationTemplateService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenTemplateNotFound() {
        when(notificationTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> notificationTemplateService.getById(1L));
        assertEquals(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getAll_ShouldReturnPageOfTemplates_WhenTemplatesExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationTemplate> templatePage = new PageImpl<>(List.of(notificationTemplate), pageable, 1);

        when(notificationTemplateRepository.findAll(pageable)).thenReturn(templatePage);

        Page<NotificationTemplateResponse> response = notificationTemplateService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void delete_ShouldDeleteTemplate_WhenTemplateExists() {
        when(notificationTemplateRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notificationTemplateRepository).deleteById(1L);

        assertDoesNotThrow(() -> notificationTemplateService.delete(1L));
        verify(notificationTemplateRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenTemplateNotFound() {
        when(notificationTemplateRepository.existsById(1L)).thenReturn(false);

        BaseException exception = assertThrows(BaseException.class, () -> notificationTemplateService.delete(1L));
        assertEquals(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findActiveByCode_ShouldReturnTemplate_WhenActiveTemplateExists() {
        when(notificationTemplateRepository.findByCodeAndChannelAndActiveTrue("WELCOME_EMAIL", NotificationChannel.EMAIL))
                .thenReturn(Optional.of(notificationTemplate));

        NotificationTemplate result = notificationTemplateService.findActiveByCode("WELCOME_EMAIL", NotificationChannel.EMAIL);

        assertNotNull(result);
        assertTrue(result.isActive());
    }

    @Test
    void findActiveByCode_ShouldThrowException_WhenTemplateNotFoundOrInactive() {
        when(notificationTemplateRepository.findByCodeAndChannelAndActiveTrue("INACTIVE", NotificationChannel.EMAIL))
                .thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> notificationTemplateService.findActiveByCode("INACTIVE", NotificationChannel.EMAIL));
        assertEquals(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE, exception.getErrorCode());
    }
}
