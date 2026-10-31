package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiPatientRequest;
import com.mb.modulith.clinic.api.response.ApiPatientResponse;
import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Patient;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientMapperTest extends BaseUnitTest {

    private final PatientMapper appointmentMapper = Mappers.getMapper(PatientMapper.class);

    @Test
    void map_ShouldMapApiPatientRequestToPatient_WhenRequestIsValid() {
        // Arrange
        ApiPatientRequest apiPatientRequest = getApiPatientRequest();

        // Act
        Patient result = appointmentMapper.map(apiPatientRequest);

        // Assertions
        assertEquals(apiPatientRequest.getName(), result.getName());
        assertEquals(apiPatientRequest.getSurname(), result.getSurname());
    }

    @Test
    void map_ShouldMapPatientToApiPatientResponse_WhenPatientIsValid() {
        // Arrange
        Patient appointment = getPatient();

        // Act
        ApiPatientResponse result = appointmentMapper.map(appointment);

        // Assertions
        assertEquals(appointment.getName(), result.getName());
        assertEquals(appointment.getSurname(), result.getSurname());
        assertEquals(appointment.getId(), result.getId());
    }

    @Test
    void map_ShouldMapPatientList_WhenListIsNotEmpty() {
        // Arrange
        List<Patient> appointments = getPatients();

        // Act
        List<ApiPatientResponse> result = appointmentMapper.map(appointments);

        // Assertions
        assertEquals(appointments.size(), result.size());
    }
}
