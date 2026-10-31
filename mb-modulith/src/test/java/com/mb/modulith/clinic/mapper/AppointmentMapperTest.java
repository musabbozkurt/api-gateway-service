package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiAppointmentRequest;
import com.mb.modulith.clinic.api.response.ApiAppointmentResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppointmentMapperTest extends BaseUnitTest {

    private final AppointmentMapper appointmentMapper = Mappers.getMapper(AppointmentMapper.class);

    @Test
    void map_ShouldMapApiAppointmentRequestToAppointment_WhenRequestIsValid() {
        // Arrange
        ApiAppointmentRequest apiAppointmentRequest = getApiAppointmentRequest();

        // Act
        Appointment result = appointmentMapper.map(apiAppointmentRequest);

        // Assertions
        assertEquals(apiAppointmentRequest.getDoctorId(), result.getDoctorId());
        assertEquals(apiAppointmentRequest.getPatientId(), result.getPatientId());
        assertEquals(apiAppointmentRequest.getStartDate(), result.getStartDate());
        assertEquals(apiAppointmentRequest.getEndDate(), result.getEndDate());
    }

    @Test
    void map_ShouldMapAppointmentToApiAppointmentResponse_WhenAppointmentIsValid() {
        // Arrange
        Appointment appointment = getAppointment();

        // Act
        ApiAppointmentResponse result = appointmentMapper.map(appointment);

        // Assertions
        assertEquals(appointment.getFee(), result.getFee());
        assertEquals(appointment.getEndDate(), result.getEndDate());
        assertEquals(appointment.getRefundedFee(), result.getRefundedFee());
        assertEquals(appointment.getStatus(), result.getStatus());
    }

    @Test
    void map_ShouldMapAppointmentList_WhenListIsNotEmpty() {
        // Arrange
        List<Appointment> appointments = getAllAppointments();

        // Act
        List<ApiAppointmentResponse> result = appointmentMapper.map(appointments);

        // Assertions
        assertEquals(appointments.size(), result.size());
    }
}
