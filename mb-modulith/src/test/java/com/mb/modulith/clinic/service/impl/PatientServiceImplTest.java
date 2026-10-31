package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Patient;
import com.mb.modulith.clinic.data.repository.PatientRepository;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest extends BaseUnitTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void createPatient_ShouldPersist_WhenPatientIsValid() {
        // Arrange
        Patient patient = getPatient();
        when(patientRepository.save(patient)).thenReturn(patient);

        // Act
        Patient result = patientService.createPatient(patient);

        // Assertions
        assertEquals(patient, result);
    }

    @Test
    void getAllPatients_ShouldReturnList_WhenPatientsExist() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(getPatients());

        // Act
        List<Patient> result = patientService.getAllPatients();

        // Assertions
        assertEquals(2, result.size());
    }

    @Test
    void getPatientById_ShouldReturnPatient_WhenFound() {
        // Arrange
        Patient patient = getPatient();
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.getPatientById(1);

        // Assertions
        assertEquals(patient, result);
    }

    @Test
    void getPatientById_ShouldThrow_WhenNotFound() {
        // Arrange
        when(patientRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> patientService.getPatientById(99));
    }

    @Test
    void deletePatientById_ShouldRemovePatient_WhenPatientExists() {
        // Arrange
        Patient patient = getPatient();
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        // Act
        patientService.deletePatientById(1);

        // Assertions
        verify(patientRepository).deleteById(1);
    }
}