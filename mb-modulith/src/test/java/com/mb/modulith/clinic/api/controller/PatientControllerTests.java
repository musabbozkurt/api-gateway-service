package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.Context;
import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiPatientRequest;
import com.mb.modulith.clinic.api.response.ApiPatientResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Patient;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.exception.RestResponseExceptionHandler;
import com.mb.modulith.clinic.mapper.PatientMapper;
import com.mb.modulith.clinic.service.PatientService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = PatientController.class)
@ContextConfiguration(classes = {PatientController.class, RestResponseExceptionHandler.class})
class PatientControllerTests extends BaseUnitTest {

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private PatientMapper patientMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void getAllPatients_ShouldReturn200_WhenPatientsExist() throws Exception {
        // Arrange
        List<Patient> patients = getPatients();
        List<ApiPatientResponse> apiPatientResponses = getApiPatientResponses();
        given(patientService.getAllPatients()).willReturn(patients);
        given(patientMapper.map(patients)).willReturn(apiPatientResponses);

        // Act
        mockMvc.perform(get("/all"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createPatient_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiPatientRequest apiPatientRequest = getApiPatientRequest();
        Patient patient = getPatient();
        ApiPatientResponse apiPatientResponse = getApiPatientResponse();
        when(patientMapper.map(any(ApiPatientRequest.class))).thenReturn(patient);
        when(patientService.createPatient(any(Patient.class))).thenReturn(patient);
        when(patientMapper.map(any(Patient.class))).thenReturn(apiPatientResponse);

        // Act
        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiPatientRequest)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getPatientById_ShouldReturn200_WhenPatientExists() throws Exception {
        // Arrange
        Patient patient = getPatient();
        int patientId = patient.getId();
        ContextHolder.set(Context.builder().patientId(patientId).build());
        ApiPatientResponse apiPatientResponse = getApiPatientResponse();
        when(patientService.getPatientById(patientId)).thenReturn(patient);
        when(patientMapper.map(patient)).thenReturn(apiPatientResponse);

        // Act
        mockMvc.perform(get("/patient"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getPatientById_ShouldReturn404_WhenPatientNotFound() throws Exception {
        // Arrange
        int id = 1;
        ContextHolder.set(Context.builder().patientId(id).build());
        when(patientService.getPatientById(id)).thenThrow(new BaseException(ClinicServiceErrorCode.NOT_FOUND));

        // Act
        mockMvc.perform(get("/patient"))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }

    @Test
    void updatePatientById_ShouldReturn200_WhenPatientExists() throws Exception {
        // Arrange
        Patient patient = getPatient();
        ApiPatientRequest apiPatientRequest = getApiPatientRequest();
        int patientId = patient.getId();
        ContextHolder.set(Context.builder().patientId(patientId).build());
        ApiPatientResponse apiPatientResponse = getApiPatientResponse();
        when(patientMapper.map(any(ApiPatientRequest.class))).thenReturn(patient);
        when(patientService.updatePatientById(eq(patientId), any(Patient.class))).thenReturn(patient);
        when(patientMapper.map(any(Patient.class))).thenReturn(apiPatientResponse);

        // Act
        mockMvc.perform(put("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiPatientRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void updatePatientById_ShouldReturn404_WhenPatientNotFound() throws Exception {
        // Arrange
        int id = 1;
        ContextHolder.set(Context.builder().patientId(id).build());
        ApiPatientRequest apiPatientRequest = getApiPatientRequest();
        when(patientMapper.map(any(ApiPatientRequest.class))).thenReturn(getPatient());
        when(patientService.updatePatientById(eq(id), any(Patient.class))).thenThrow(new BaseException(ClinicServiceErrorCode.NOT_FOUND));

        // Act
        mockMvc.perform(put("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiPatientRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }
}
