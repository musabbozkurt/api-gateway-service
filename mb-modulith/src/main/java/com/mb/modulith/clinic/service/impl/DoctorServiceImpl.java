package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.data.entity.Doctor;
import com.mb.modulith.clinic.data.repository.DoctorRepository;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.service.DoctorService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor getDoctorById(int doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BaseException(ClinicServiceErrorCode.NOT_FOUND));
    }

    @Override
    public Doctor updateDoctorById(int doctorId, Doctor doctor) {
        Doctor existingDoctor = getDoctorById(doctorId);
        existingDoctor.setPerHourFee(doctor.getPerHourFee());
        return doctorRepository.save(existingDoctor);
    }

    @Override
    public void deleteDoctorById(int doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        doctorRepository.deleteById(doctor.getId());
    }

}
