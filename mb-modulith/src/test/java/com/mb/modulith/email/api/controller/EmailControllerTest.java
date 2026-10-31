package com.mb.modulith.email.api.controller;

import com.mb.modulith.email.api.dto.request.ApiEmailRequest;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.exception.EmailErrorCode;
import com.mb.modulith.email.mapping.EmailMapper;
import com.mb.modulith.email.service.EmailService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = EmailController.class)
@ContextConfiguration(classes = EmailController.class)
class EmailControllerTest {

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private EmailMapper emailMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEmails_ShouldReturn200_WhenEmailsExist() throws Exception {
        // Arrange
        when(emailService.findAll()).thenReturn(List.of(new Email()));
        when(emailMapper.map(anyList())).thenReturn(List.of(ApiEmailResponse.builder().build()));

        // Act
        mockMvc.perform(get("/api/emails"))
                .andExpect(status().isOk());

        // Assertions
        verify(emailService).findAll();
    }

    @Test
    void getEmailById_ShouldReturn200_WhenEmailExists() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Email email = new Email();
        when(emailService.findById(id)).thenReturn(email);
        when(emailMapper.map(email)).thenReturn(ApiEmailResponse.builder().build());

        // Act
        mockMvc.perform(get("/api/emails/{id}", id))
                .andExpect(status().isOk());

        // Assertions
        verify(emailService).findById(id);
    }

    @Test
    void createEmail_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiEmailRequest request = new ApiEmailRequest();
        Email email = new Email();
        when(emailMapper.map(any(ApiEmailRequest.class))).thenReturn(email);
        when(emailService.save(email)).thenReturn(email);
        when(emailMapper.map(email)).thenReturn(ApiEmailResponse.builder().build());

        // Act
        mockMvc.perform(post("/api/emails")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assertions
        verify(emailService).save(email);
    }

    @Test
    void deleteEmail_ShouldReturn204_WhenDeleteSucceeds() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        mockMvc.perform(delete("/api/emails/{id}", id))
                .andExpect(status().isNoContent());

        // Assertions
        verify(emailService).deleteById(id);
    }

    @Test
    void deleteEmail_ShouldReturn417_WhenDeleteFails() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        doThrow(new BaseException(EmailErrorCode.UNEXPECTED_ERROR, "fail")).when(emailService).deleteById(id);

        // Act
        mockMvc.perform(delete("/api/emails/{id}", id))
                .andExpect(status().isExpectationFailed());

        // Assertions
        verify(emailService).deleteById(id);
    }

    @Test
    void getEmailCountByEmailAddress_ShouldReturn204_WhenNotFound() throws Exception {
        // Arrange
        when(emailService.getEmailCountByEmailAddress("missing@test.com")).thenReturn(null);

        // Act
        mockMvc.perform(get("/api/emails/count/{emailAddress}", "missing@test.com"))
                .andExpect(status().isNoContent());

        // Assertions
        verify(emailService).getEmailCountByEmailAddress("missing@test.com");
    }

    @Test
    void updateEmail_ShouldReturn200_WhenRequestIsValid() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        ApiEmailRequest request = new ApiEmailRequest();
        Email email = new Email();
        when(emailMapper.map(any(ApiEmailRequest.class))).thenReturn(email);
        when(emailService.updateEmail(id, email)).thenReturn(email);
        when(emailMapper.map(email)).thenReturn(ApiEmailResponse.builder().build());

        // Act
        mockMvc.perform(put("/api/emails/{id}", id)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assertions
        verify(emailService).updateEmail(id, email);
    }
}
