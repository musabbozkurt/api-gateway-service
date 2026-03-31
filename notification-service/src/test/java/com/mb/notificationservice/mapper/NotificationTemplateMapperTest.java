package com.mb.notificationservice.mapper;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateMapperTest {

    private NotificationTemplateMapper converter;

    @BeforeEach
    void setUp() {
        converter = new NotificationTemplateMapper();
    }

    @Test
    void convert_ShouldMapAllFields_WhenConvertingFromRequest() {
        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setCode("TEST_CODE");
        request.setName("Test Name");
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        request.setDescription("Test Description");
        request.setActive(true);

        NotificationTemplate result = converter.convert(request);

        assertNotNull(result);
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals("TEST_CODE", result.getCode());
        assertEquals("Test Name", result.getName());
        assertEquals("Test Subject", result.getSubject());
        assertEquals("Test Body", result.getBody());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.isActive());
    }

    @Test
    void convert_ShouldSetInactiveStatus_WhenRequestHasInactiveTemplate() {
        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setCode("TEST_CODE");
        request.setBody("Test Body");
        request.setActive(false);

        NotificationTemplate result = converter.convert(request);

        assertNotNull(result);
        assertFalse(result.isActive());
    }

    @Test
    void convert_ShouldHandleNullOptionalFields_WhenConvertingFromRequest() {
        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setCode("TEST_CODE");
        request.setBody("Test Body");
        request.setName(null);
        request.setSubject(null);
        request.setDescription(null);

        NotificationTemplate result = converter.convert(request);

        assertNotNull(result);
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals("TEST_CODE", result.getCode());
        assertNull(result.getName());
        assertNull(result.getSubject());
        assertNull(result.getDescription());
    }

    @Test
    void update_ShouldUpdateAllFields_WhenRequestContainsNewValues() {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setChannel(NotificationChannel.EMAIL);
        template.setCode("OLD_CODE");
        template.setName("Old Name");
        template.setActive(false);

        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setCode("NEW_CODE");
        request.setName("New Name");
        request.setSubject("New Subject");
        request.setBody("New Body");
        request.setDescription("New Description");
        request.setActive(true);

        converter.update(template, request);

        assertEquals(1L, template.getId());
        assertEquals(NotificationChannel.SMS, template.getChannel());
        assertEquals("NEW_CODE", template.getCode());
        assertEquals("New Name", template.getName());
        assertEquals("New Subject", template.getSubject());
        assertEquals("New Body", template.getBody());
        assertEquals("New Description", template.getDescription());
        assertTrue(template.isActive());
    }

    @Test
    void convert_ShouldMapAllFields_WhenConvertingFromEntity() {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setChannel(NotificationChannel.EMAIL);
        template.setCode("TEST_CODE");
        template.setName("Test Name");
        template.setSubject("Test Subject");
        template.setBody("Test Body");
        template.setDescription("Test Description");
        template.setActive(true);

        NotificationTemplateResponse result = converter.convert(template);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(NotificationChannel.EMAIL, result.getChannel());
        assertEquals("TEST_CODE", result.getCode());
        assertEquals("Test Name", result.getName());
        assertEquals("Test Subject", result.getSubject());
        assertEquals("Test Body", result.getBody());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.isActive());
    }

    @Test
    void convert_ShouldMapAuditFields_WhenEntityHasAuditData() {
        LocalDateTime createdDate = LocalDateTime.of(2026, 1, 15, 10, 30);
        LocalDateTime modifiedDate = LocalDateTime.of(2026, 1, 20, 14, 45);

        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setChannel(NotificationChannel.EMAIL);
        template.setCode("TEST_CODE");
        template.setBody("Test Body");
        template.setCreatedBy("admin");
        template.setCreatedDate(createdDate);
        template.setLastModifiedBy("admin");
        template.setLastModifiedDate(modifiedDate);

        NotificationTemplateResponse result = converter.convert(template);

        assertNotNull(result);
        assertEquals("admin", result.getCreatedBy());
        assertEquals(createdDate, result.getCreatedDate());
        assertEquals("admin", result.getLastModifiedBy());
        assertEquals(modifiedDate, result.getLastModifiedDate());
    }

    @Test
    void convert_ShouldHandleNullAuditFields_WhenEntityHasNoAuditData() {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setChannel(NotificationChannel.EMAIL);
        template.setCode("TEST_CODE");
        template.setBody("Test Body");

        NotificationTemplateResponse result = converter.convert(template);

        assertNotNull(result);
        assertNull(result.getCreatedBy());
        assertNull(result.getCreatedDate());
        assertNull(result.getLastModifiedBy());
        assertNull(result.getLastModifiedDate());
    }

    @Test
    void convert_ShouldSetInactiveStatus_WhenEntityIsInactive() {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setChannel(NotificationChannel.EMAIL);
        template.setCode("TEST_CODE");
        template.setBody("Test Body");
        template.setActive(false);

        NotificationTemplateResponse result = converter.convert(template);

        assertNotNull(result);
        assertFalse(result.isActive());
    }
}
