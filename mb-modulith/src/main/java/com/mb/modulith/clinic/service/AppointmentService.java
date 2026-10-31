package com.mb.modulith.clinic.service;

import com.mb.modulith.clinic.data.entity.Appointment;

import java.util.List;

public interface AppointmentService {

    List<Appointment> getAllAppointmentsByUserId(Integer userId);

    Appointment createAppointment(Appointment appointment);

    Appointment cancelAppointmentById(int appointmentId);

}
