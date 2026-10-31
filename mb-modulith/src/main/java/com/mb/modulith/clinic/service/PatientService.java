package com.mb.modulith.clinic.service;

import com.mb.modulith.clinic.data.entity.Patient;

import java.util.List;

public interface PatientService {

    Patient createPatient(Patient patient);

    List<Patient> getAllPatients();

    Patient getPatientById(int patientId);

    Patient updatePatientById(int patientId, Patient patient);

    void deletePatientById(int patientId);
}
