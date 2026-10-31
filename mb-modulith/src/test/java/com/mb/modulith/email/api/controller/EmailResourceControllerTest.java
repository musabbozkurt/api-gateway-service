package com.mb.modulith.email.api.controller;

import com.google.gson.Gson;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.mapping.EmailMapper;
import com.mb.modulith.email.service.EmailResourceService;
import com.mb.modulith.email.service.EmailService;
import com.mb.modulith.email.service.FileIOService;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = EmailResourceController.class)
@ContextConfiguration(classes = EmailResourceController.class)
class EmailResourceControllerTest {

    @MockitoBean
    private EmailResourceService emailResourceService;

    @MockitoBean
    private EmailMapper emailMapper;

    @MockitoBean
    private JobOperator jobOperator;

    @MockitoBean(name = "exampleJob")
    private Job job;

    @MockitoBean
    private Gson gson;

    @MockitoBean
    private FileIOService fileIOService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDummyDataSetResponseDto_ShouldReturn200_WhenServiceReturnsDto() throws Exception {
        // Arrange
        DataSetResponseDto dto = new DataSetResponseDto();
        when(emailResourceService.generateDummyDataSetResponseDto()).thenReturn(dto);
        when(emailMapper.map(dto)).thenReturn(dto);

        // Act
        mockMvc.perform(get("/client/emails/data-set"))
                .andExpect(status().isOk());

        // Assertions
        verify(emailResourceService).generateDummyDataSetResponseDto();
    }

    @Test
    void getDummyDataSetResponseDto1_ShouldReturn200_WhenServiceReturnsDto() throws Exception {
        // Arrange
        DataSetResponseDto dto = new DataSetResponseDto();
        when(emailResourceService.generateDummyDataSetResponseDto1()).thenReturn(dto);
        when(emailMapper.map(dto)).thenReturn(dto);

        // Act
        mockMvc.perform(get("/client/emails/data-set-1"))
                .andExpect(status().isOk());

        // Assertions
        verify(emailResourceService).generateDummyDataSetResponseDto1();
    }

    @Test
    void getDummyDataSetResponseDto2_ShouldReturn200_WhenServiceReturnsDto() throws Exception {
        // Arrange
        DataSetResponseDto dto = new DataSetResponseDto();
        when(emailResourceService.generateDummyDataSetResponseDto2()).thenReturn(dto);
        when(emailMapper.map(dto)).thenReturn(dto);

        // Act
        mockMvc.perform(get("/client/emails/data-set-2"))
                .andExpect(status().isOk());

        // Assertions
        verify(emailResourceService).generateDummyDataSetResponseDto2();
    }

    @Test
    void checkEmails_ShouldReturn200_WhenBatchRuns() throws Exception {
        // Arrange
        DataSetResponseDto dto = new DataSetResponseDto();
        when(gson.toJson(any(DataSetResponseDto.class))).thenReturn("{}");
        when(emailService.findAll()).thenReturn(List.of());
        when(emailMapper.map(anyList())).thenReturn(List.of(ApiEmailResponse.builder().build()));

        // Act
        mockMvc.perform(post("/client/emails/data-set")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assertions
        verify(fileIOService).writeDataSetResponseDtoToFile("{}");
        verify(jobOperator).run(any(), any());
        verify(emailService).findAll();
    }
}
