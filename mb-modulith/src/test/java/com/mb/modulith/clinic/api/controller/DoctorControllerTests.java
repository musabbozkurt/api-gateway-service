package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.Context;
import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiDoctorRequest;
import com.mb.modulith.clinic.api.response.ApiDoctorResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Doctor;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.exception.RestResponseExceptionHandler;
import com.mb.modulith.clinic.mapper.DoctorMapper;
import com.mb.modulith.clinic.service.DoctorService;
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
@WebMvcTest(controllers = DoctorController.class)
@ContextConfiguration(classes = {DoctorController.class, RestResponseExceptionHandler.class})
class DoctorControllerTests extends BaseUnitTest {
    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private DoctorMapper doctorMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void getAllDoctors_ShouldReturn200_WhenDoctorsExist() throws Exception {
        // Arrange
        List<Doctor> doctors = getDoctors();
        List<ApiDoctorResponse> apiDoctorResponses = getApiDoctorResponses();
        given(doctorService.getAllDoctors()).willReturn(doctors);
        given(doctorMapper.map(doctors)).willReturn(apiDoctorResponses);

        // Act
        mockMvc.perform(get("/doctor"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void createDoctor_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiDoctorRequest apiDoctorRequest = getApiDoctorRequest();
        Doctor doctor = getDoctor();
        ApiDoctorResponse apiDoctorResponse = getApiDoctorResponse();
        when(doctorMapper.map(any(ApiDoctorRequest.class))).thenReturn(doctor);
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.map(any(Doctor.class))).thenReturn(apiDoctorResponse);

        // Act
        mockMvc.perform(post("/doctor").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiDoctorRequest)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getDoctorById_ShouldReturn200_WhenDoctorExists() throws Exception {
        // Arrange
        Doctor doctor = getDoctor();
        int doctorId = doctor.getId();
        ContextHolder.set(Context.builder().doctorId(doctorId).build());
        ApiDoctorResponse apiDoctorResponse = getApiDoctorResponse();
        when(doctorService.getDoctorById(doctorId)).thenReturn(doctor);
        when(doctorMapper.map(doctor)).thenReturn(apiDoctorResponse);

        // Act
        mockMvc.perform(get("/doctor"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getDoctorById_ShouldReturn404_WhenDoctorNotFound() throws Exception {
        // Arrange
        int id = 1;
        ContextHolder.set(Context.builder().doctorId(id).build());
        when(doctorService.getDoctorById(id)).thenThrow(new BaseException(ClinicServiceErrorCode.NOT_FOUND));

        // Act
        mockMvc.perform(get("/doctor"))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }

    @Test
    void updateDoctorById_ShouldReturn200_WhenDoctorExists() throws Exception {
        // Arrange
        Doctor doctor = getDoctor();
        ContextHolder.set(Context.builder().doctorId(doctor.getId()).build());
        ApiDoctorRequest apiDoctorRequest = getApiDoctorRequest();
        int doctorId = doctor.getId();
        ApiDoctorResponse apiDoctorResponse = getApiDoctorResponse();
        when(doctorMapper.map(any(ApiDoctorRequest.class))).thenReturn(doctor);
        when(doctorService.updateDoctorById(eq(doctorId), any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.map(any(Doctor.class))).thenReturn(apiDoctorResponse);

        // Act
        mockMvc.perform(put("/doctor").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiDoctorRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void updateDoctorById_ShouldReturn404_WhenDoctorNotFound() throws Exception {
        // Arrange
        int id = 1;
        ContextHolder.set(Context.builder().doctorId(id).build());
        Doctor doctor = getDoctor();
        ApiDoctorRequest apiDoctorRequest = getApiDoctorRequest();
        when(doctorMapper.map(any(ApiDoctorRequest.class))).thenReturn(doctor);
        when(doctorService.updateDoctorById(eq(id), any(Doctor.class))).thenThrow(new BaseException(ClinicServiceErrorCode.NOT_FOUND));

        // Act
        mockMvc.perform(put("/doctor").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiDoctorRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }
}
