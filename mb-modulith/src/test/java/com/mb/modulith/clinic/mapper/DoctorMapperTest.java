package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiDoctorRequest;
import com.mb.modulith.clinic.api.response.ApiDoctorResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Doctor;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoctorMapperTest extends BaseUnitTest {

    private final DoctorMapper appointmentMapper = Mappers.getMapper(DoctorMapper.class);

    @Test
    void map_ShouldMapApiDoctorRequestToDoctor_WhenRequestIsValid() {
        // Arrange
        ApiDoctorRequest apiDoctorRequest = getApiDoctorRequest();

        // Act
        Doctor result = appointmentMapper.map(apiDoctorRequest);

        // Assertions
        assertEquals(apiDoctorRequest.getName(), result.getName());
        assertEquals(apiDoctorRequest.getPerHourFee(), result.getPerHourFee());
        assertEquals(apiDoctorRequest.getSurname(), result.getSurname());
    }

    @Test
    void map_ShouldMapDoctorToApiDoctorResponse_WhenDoctorIsValid() {
        // Arrange
        Doctor appointment = getDoctor();

        // Act
        ApiDoctorResponse result = appointmentMapper.map(appointment);

        // Assertions
        assertEquals(appointment.getName(), result.getName());
        assertEquals(appointment.getPerHourFee(), result.getPerHourFee());
        assertEquals(appointment.getSurname(), result.getSurname());
        assertEquals(appointment.getId(), result.getId());
    }

    @Test
    void map_ShouldMapDoctorList_WhenListIsNotEmpty() {
        // Arrange
        List<Doctor> appointments = getDoctors();

        // Act
        List<ApiDoctorResponse> result = appointmentMapper.map(appointments);

        // Assertions
        assertEquals(appointments.size(), result.size());
    }
}
