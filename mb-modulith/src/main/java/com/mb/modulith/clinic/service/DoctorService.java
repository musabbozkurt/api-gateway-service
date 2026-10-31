package com.mb.modulith.clinic.service;

import com.mb.modulith.clinic.data.entity.Doctor;

import java.util.List;

public interface DoctorService {

    List<Doctor> getAllDoctors();

    Doctor createDoctor(Doctor doctor);

    Doctor getDoctorById(int doctorId);

    Doctor updateDoctorById(int doctorId, Doctor doctorRequest);

    void deleteDoctorById(int doctorId);
}
