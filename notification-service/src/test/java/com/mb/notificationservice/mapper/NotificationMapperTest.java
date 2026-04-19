package com.mb.notificationservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationDetailResponse;
import com.mb.notificationservice.api.response.NotificationSummaryResponse;
import com.mb.notificationservice.data.entity.Notification;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.enums.NotificationLevel;
import com.mb.notificationservice.enums.NotificationStatus;
import com.mb.notificationservice.enums.NotificationType;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationMapperTest {

    @InjectMocks
    private NotificationMapper converter;

    @Spy
    @SuppressWarnings("unused")
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ContextHolder.setContext(ContextHolder.Context.builder().userId(99L).build());
    }

    @Test
    void convertRequest_ShouldMapAllFields_WhenRequestIsFullyPopulated() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setLevel(NotificationLevel.WARNING);
        request.setSubject("Subject");
        request.setBody("Body");
        request.setTitle("Title");
        request.setTemplateCode("TMPL");
        request.setTemplateParameters(Map.of("key", "value"));
        request.setData(Map.of("k", "v"));
        request.setUserId(10L);
        request.setRecipients(Set.of("r@test.com"));
        request.setCc(Set.of("cc@test.com"));
        request.setBcc(Set.of("bcc@test.com"));

        NotificationEventDto dto = converter.convert(request);

        assertNotNull(dto.getId());
        assertEquals(NotificationChannel.EMAIL, dto.getChannel());
        assertEquals(NotificationLevel.WARNING, dto.getLevel());
        assertEquals("Subject", dto.getSubject());
        assertEquals("Body", dto.getBody());
        assertEquals("Title", dto.getTitle());
        assertEquals("TMPL", dto.getTemplateCode());
        assertEquals(Map.of("key", "value"), dto.getTemplateParameters());
        assertEquals(Map.of("k", "v"), dto.getData());
        assertEquals(10L, dto.getUserId());
        assertEquals(Set.of("r@test.com"), dto.getRecipients());
        assertEquals(Set.of("cc@test.com"), dto.getCc());
        assertEquals(Set.of("bcc@test.com"), dto.getBcc());
        assertEquals(99L, dto.getCreatedBy());
    }

    @Test
    void convertDto_ShouldMapAllFields_WhenDtoIsFullyPopulated() {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.SMS);
        dto.setLevel(NotificationLevel.ERROR);
        dto.setSubject("Sub");
        dto.setBody("Bod");
        dto.setTitle("Ttl");
        dto.setTemplateCode("CODE");
        dto.setTemplateParameters(Map.of("p", "v"));
        dto.setData(Map.of("d", "val"));
        dto.setUserId(5L);
        dto.setRecipients(Set.of("905551234567"));
        dto.setCc(Set.of("cc"));
        dto.setBcc(Set.of("bcc"));
        dto.setCreatedBy(42L);

        Notification entity = converter.convert(dto);

        assertEquals(NotificationChannel.SMS, entity.getChannel());
        assertEquals(NotificationLevel.ERROR, entity.getLevel());
        assertEquals("Sub", entity.getSubject());
        assertEquals("Bod", entity.getBody());
        assertEquals("Ttl", entity.getTitle());
        assertEquals("CODE", entity.getTemplateCode());
        assertNotNull(entity.getTemplateParameters());
        assertNotNull(entity.getData());
        assertEquals(5L, entity.getUserId());
        assertEquals(Set.of("905551234567"), entity.getRecipients());
        assertEquals(Set.of("cc"), entity.getCc());
        assertEquals(Set.of("bcc"), entity.getBcc());
        assertEquals("42", entity.getCreatedBy());
    }

    @Test
    void convertDto_ShouldUseDefaults_WhenCollectionsAreNull() {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.PUSH);
        dto.setRecipients(null);
        dto.setCc(null);
        dto.setBcc(null);
        dto.setTemplateParameters(null);
        dto.setData(null);
        dto.setCreatedBy(1L);

        Notification entity = converter.convert(dto);

        assertEquals(Set.of(), entity.getRecipients());
        assertEquals(Set.of(), entity.getCc());
        assertEquals(Set.of(), entity.getBcc());
        assertNull(entity.getTemplateParameters());
        assertNull(entity.getData());
    }

    @Test
    void convertDto_ShouldSkipSerialization_WhenCollectionsAreEmpty() {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.PUSH);
        dto.setTemplateParameters(Map.of());
        dto.setData(Map.of());
        dto.setCreatedBy(1L);

        Notification entity = converter.convert(dto);

        assertNull(entity.getTemplateParameters());
        assertNull(entity.getData());
    }

    @Test
    void convertDto_ShouldHandleGracefully_WhenJsonProcessingFails() throws JsonProcessingException {
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        NotificationMapper converterWithBrokenMapper = new NotificationMapper(brokenMapper);

        when(brokenMapper.writeValueAsString(Map.of("k", "v"))).thenThrow(new JsonProcessingException("fail") {
        });

        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.EMAIL);
        dto.setTemplateParameters(Map.of("k", "v"));
        dto.setData(Map.of("k", "v"));
        dto.setCreatedBy(1L);

        Notification entity = converterWithBrokenMapper.convert(dto);

        assertNull(entity.getTemplateParameters());
        assertNull(entity.getData());
    }

    @Test
    void toNotificationSummaryResponse_ShouldMapAllFields_WhenEntityIsPopulated() {
        Notification entity = createNotification();

        NotificationSummaryResponse response = converter.toNotificationSummaryResponse(entity);

        assertEquals(1L, response.getId());
        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertEquals("Subject", response.getSubject());
        assertEquals("Title", response.getTitle());
        assertEquals(NotificationLevel.INFO, response.getLevel());
        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertFalse(response.isRead());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void toNotificationSummaryResponse_ShouldReturnNullCreatedAt_WhenCreatedDateIsNull() {
        Notification entity = createNotification();
        entity.setCreatedDate(null);

        NotificationSummaryResponse response = converter.toNotificationSummaryResponse(entity);

        assertNull(response.getCreatedAt());
    }

    @Test
    void toNotificationDetailResponse_ShouldMapAllFields_WhenEntityIsPopulated() {
        Notification entity = createNotification();
        entity.setData("{\"orderId\":\"123\"}");
        entity.setRecipients(Set.of("a@test.com", "b@test.com"));
        entity.setCc(Set.of("cc@test.com"));
        entity.setBcc(Set.of("bcc@test.com"));

        NotificationDetailResponse response = converter.toNotificationDetailResponse(entity);

        assertEquals(1L, response.getId());
        assertEquals(NotificationChannel.PUSH, response.getChannel());
        assertEquals(NotificationLevel.INFO, response.getLevel());
        assertEquals(NotificationType.SYSTEM, response.getType());
        assertEquals("Subject", response.getSubject());
        assertEquals("Body", response.getBody());
        assertEquals("Title", response.getTitle());
        assertEquals(Map.of("orderId", "123"), response.getData());
        assertEquals(Set.of("a@test.com", "b@test.com"), response.getRecipients());
        assertEquals(Set.of("cc@test.com"), response.getCc());
        assertEquals(Set.of("bcc@test.com"), response.getBcc());
        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertFalse(response.isRead());
    }

    @Test
    void toNotificationDetailResponse_ShouldReturnEmptyCollections_WhenFieldsAreNullOrBlank() {
        Notification entity = createNotification();
        entity.setData(null);
        entity.setRecipients(Set.of());
        entity.setCc(Set.of());
        entity.setBcc(Set.of());

        NotificationDetailResponse response = converter.toNotificationDetailResponse(entity);

        assertTrue(response.getData().isEmpty());
        assertTrue(response.getRecipients().isEmpty());
        assertTrue(response.getCc().isEmpty());
        assertTrue(response.getBcc().isEmpty());
    }

    @Test
    void toNotificationDetailResponse_ShouldReturnEmptyData_WhenJsonIsInvalid() {
        Notification entity = createNotification();
        entity.setData("not-valid-json");

        NotificationDetailResponse response = converter.toNotificationDetailResponse(entity);

        assertTrue(response.getData().isEmpty());
    }

    @Test
    void toNotificationDetailResponse_ShouldReturnNullCreatedAt_WhenCreatedDateIsNull() {
        Notification entity = createNotification();
        entity.setCreatedDate(null);

        NotificationDetailResponse response = converter.toNotificationDetailResponse(entity);

        assertNull(response.getCreatedAt());
    }

    @Test
    void toRequest_ShouldMapAllFields_WhenDtoIsFullyPopulated() {
        NotificationEventDto dto = new NotificationEventDto();
        dto.setChannel(NotificationChannel.EMAIL);
        dto.setLevel(NotificationLevel.SUCCESS);
        dto.setSubject("Subj");
        dto.setBody("Bdy");
        dto.setTitle("Ttl");
        dto.setTemplateCode("T1");
        dto.setTemplateParameters(Map.of("k", "v"));
        dto.setData(Map.of("d", "val"));
        dto.setUserId(7L);
        dto.setRecipients(Set.of("r@test.com"));
        dto.setCc(Set.of("c@test.com"));
        dto.setBcc(Set.of("b@test.com"));

        NotificationRequest request = converter.toRequest(dto);

        assertEquals(NotificationChannel.EMAIL, request.getChannel());
        assertEquals(NotificationLevel.SUCCESS, request.getLevel());
        assertEquals("Subj", request.getSubject());
        assertEquals("Bdy", request.getBody());
        assertEquals("Ttl", request.getTitle());
        assertEquals("T1", request.getTemplateCode());
        assertEquals(Map.of("k", "v"), request.getTemplateParameters());
        assertEquals(Map.of("d", "val"), request.getData());
        assertEquals(7L, request.getUserId());
        assertEquals(Set.of("r@test.com"), request.getRecipients());
        assertEquals(Set.of("c@test.com"), request.getCc());
        assertEquals(Set.of("b@test.com"), request.getBcc());
    }

    private Notification createNotification() {
        Notification entity = new Notification();
        entity.setId(1L);
        entity.setChannel(NotificationChannel.PUSH);
        entity.setLevel(NotificationLevel.INFO);
        entity.setType(NotificationType.SYSTEM);
        entity.setSubject("Subject");
        entity.setBody("Body");
        entity.setTitle("Title");
        entity.setStatus(NotificationStatus.SENT);
        entity.setRead(false);
        entity.setReadAt(null);

        entity.setCreatedDate(LocalDateTime.of(2026, 3, 30, 10, 0));

        return entity;
    }
}
