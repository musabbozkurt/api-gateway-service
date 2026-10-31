package com.mb.modulith.clinic.api.controller;

import com.mb.modulith.clinic.api.core.ContextHolder;
import com.mb.modulith.clinic.api.request.ApiAppointmentRequest;
import com.mb.modulith.clinic.api.response.ApiAppointmentResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Appointment;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.exception.RestResponseExceptionHandler;
import com.mb.modulith.clinic.mapper.AppointmentMapper;
import com.mb.modulith.clinic.service.AppointmentService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.support.JacksonTestConfiguration;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = AppointmentController.class)
@ContextConfiguration(classes = {AppointmentController.class, RestResponseExceptionHandler.class})
class AppointmentControllerTests extends BaseUnitTest {

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private AppointmentMapper appointmentMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAppointment_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiAppointmentRequest apiAppointmentRequest = getApiAppointmentRequest();
        Appointment appointment = getAppointment();
        ApiAppointmentResponse apiAppointmentResponse = getApiAppointmentResponse();
        when(appointmentMapper.map(apiAppointmentRequest)).thenReturn(appointment);
        when(appointmentService.createAppointment(appointment)).thenReturn(appointment);
        when(appointmentMapper.map(appointment)).thenReturn(apiAppointmentResponse);

        // Act
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiAppointmentRequest)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getAllAppointmentsByUserId_ShouldReturn200_WhenAppointmentsExist() throws Exception {
        // Arrange
        List<Appointment> allAppointments = getAllAppointments();
        List<ApiAppointmentResponse> allApiAppointmentResponses = getAllApiAppointmentResponses();
        given(appointmentService.getAllAppointmentsByUserId(ContextHolder.get().getUserId())).willReturn(allAppointments);
        given(appointmentMapper.map(allAppointments)).willReturn(allApiAppointmentResponses);

        // Act
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void cancelAppointmentById_ShouldReturn200_WhenAppointmentExists() throws Exception {
        // Arrange
        Appointment appointment = getAppointment();
        int appointmentId = appointment.getId();
        ApiAppointmentResponse apiAppointmentResponse = getApiAppointmentResponse();
        when(appointmentService.cancelAppointmentById(appointmentId)).thenReturn(appointment);
        when(appointmentMapper.map(appointment)).thenReturn(apiAppointmentResponse);

        // Act
        mockMvc.perform(put("/appointments")
                        .param("appointmentId", String.valueOf(appointmentId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(apiAppointmentResponse.getId()))
                .andExpect(jsonPath("$.fee").value(apiAppointmentResponse.getFee()));
    }

    @Test
    void cancelAppointmentById_ShouldReturn404_WhenAppointmentNotFound() throws Exception {
        // Arrange
        int id = 1;
        when(appointmentService.cancelAppointmentById(id)).thenThrow(new BaseException(ClinicServiceErrorCode.NOT_FOUND));

        // Act
        mockMvc.perform(put("/appointments")
                        .param("appointmentId", String.valueOf(id)))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }
}
