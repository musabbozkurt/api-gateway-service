package com.mb.modulith.clinic.service.impl;

import com.mb.modulith.clinic.data.entity.Appointment;
import com.mb.modulith.clinic.data.entity.Doctor;
import com.mb.modulith.clinic.data.repository.AppointmentRepository;
import com.mb.modulith.clinic.enums.AppointmentStatus;
import com.mb.modulith.clinic.exception.ClinicServiceErrorCode;
import com.mb.modulith.clinic.service.AppointmentService;
import com.mb.modulith.clinic.service.DoctorService;
import com.mb.modulith.clinic.service.FeeService;
import com.mb.modulith.clinic.service.PatientService;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final FeeService feeService;

    @Override
    public List<Appointment> getAllAppointmentsByUserId(Integer userId) {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        Doctor doctorById = doctorService.getDoctorById(appointment.getDoctorId());
        appointment.setDoctor(doctorById);
        appointment.setFee(doctorById.getPerHourFee());
        appointment.setPatient(patientService.getPatientById(appointment.getPatientId()));
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment cancelAppointmentById(int appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BaseException(ClinicServiceErrorCode.NOT_FOUND));
        if (Duration.between(appointment.getEndDate(), OffsetDateTime.now()).toMinutes() <= 60) {
            appointment.setRefundedFee(feeService.calculatePenaltyFee(appointment.getFee()));
        } else {
            appointment.setRefundedFee(appointment.getFee());
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return appointment;
    }

}
