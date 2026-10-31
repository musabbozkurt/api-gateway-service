package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.base.BaseUnitTest;
import com.mb.modulith.clinic.data.entity.Doctor;
import com.mb.modulith.clinic.data.repository.DoctorRepository;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest extends BaseUnitTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void createDoctor_ShouldPersist_WhenDoctorIsValid() {
        // Arrange
        Doctor doctor = getDoctor();
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // Act
        Doctor result = doctorService.createDoctor(doctor);

        // Assertions
        assertEquals(doctor, result);
    }

    @Test
    void getDoctorById_ShouldReturnDoctor_WhenFound() {
        // Arrange
        Doctor doctor = getDoctor();
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        // Act
        Doctor result = doctorService.getDoctorById(1);

        // Assertions
        assertEquals(doctor, result);
    }

    @Test
    void getDoctorById_ShouldThrow_WhenNotFound() {
        // Arrange
        when(doctorRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> doctorService.getDoctorById(99));
    }

    @Test
    void deleteDoctorById_ShouldRemoveDoctor_WhenDoctorExists() {
        // Arrange
        Doctor doctor = getDoctor();
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        // Act
        doctorService.deleteDoctorById(1);

        // Assertions
        verify(doctorRepository).deleteById(1);
    }
}