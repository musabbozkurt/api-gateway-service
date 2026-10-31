package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.data.entity.Patient;
import com.mb.modulith.clinic.data.repository.PatientRepository;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.service.PatientService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getPatientById(int patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new BaseException(ClinicServiceErrorCode.NOT_FOUND));
    }

    @Override
    public Patient updatePatientById(int patientId, Patient patient) {
        Patient existingPatient = getPatientById(patientId);
        existingPatient.setName(patient.getName());
        existingPatient.setName(patient.getSurname());
        patientRepository.save(existingPatient);
        return existingPatient;
    }

    @Override
    public void deletePatientById(int patientId) {
        Patient patientById = getPatientById(patientId);
        patientRepository.deleteById(patientById.getId());
    }

}
