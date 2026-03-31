package com.mb.notificationservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private NotificationTemplateController notificationTemplateController;

    @Mock
    private NotificationTemplateService notificationTemplateService;

    private ObjectMapper objectMapper;
    private NotificationTemplateRequest validRequest;
    private NotificationTemplateResponse templateResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationTemplateController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        validRequest = new NotificationTemplateRequest();
        validRequest.setChannel(NotificationChannel.EMAIL);
        validRequest.setCode("WELCOME_EMAIL");
        validRequest.setName("Welcome Email");
        validRequest.setSubject("Welcome!");
        validRequest.setBody("Hello, welcome!");
        validRequest.setDescription("Welcome email template");
        validRequest.setActive(true);

        templateResponse = new NotificationTemplateResponse();
        templateResponse.setId(1L);
        templateResponse.setChannel(NotificationChannel.EMAIL);
        templateResponse.setCode("WELCOME_EMAIL");
    }

    @Test
    void create_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        when(notificationTemplateService.create(any(NotificationTemplateRequest.class))).thenReturn(templateResponse);

        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(notificationTemplateService).create(any(NotificationTemplateRequest.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenChannelIsNull() throws Exception {
        validRequest.setChannel(null);

        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationTemplateService, never()).create(any());
    }

    @Test
    void update_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        when(notificationTemplateService.update(eq(1L), any(NotificationTemplateRequest.class))).thenReturn(templateResponse);

        mockMvc.perform(put("/api/v1/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(notificationTemplateService).update(eq(1L), any(NotificationTemplateRequest.class));
    }

    @Test
    void getById_ShouldReturnOk_WhenTemplateExists() throws Exception {
        when(notificationTemplateService.getById(1L)).thenReturn(templateResponse);

        mockMvc.perform(get("/api/v1/templates/1"))
                .andExpect(status().isOk());

        verify(notificationTemplateService).getById(1L);
    }

    @Test
    void getAll_ShouldReturnPageOfTemplates() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<NotificationTemplateResponse> page = new PageImpl<>(List.of(templateResponse), pageable, 1);
        when(notificationTemplateService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/templates").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());

        verify(notificationTemplateService).getAll(any(Pageable.class));
    }

    @Test
    void delete_ShouldReturnOk_WhenTemplateExists() throws Exception {
        doNothing().when(notificationTemplateService).delete(1L);

        mockMvc.perform(delete("/api/v1/templates/1"))
                .andExpect(status().isOk());

        verify(notificationTemplateService).delete(1L);
    }
}
