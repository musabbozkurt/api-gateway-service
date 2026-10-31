package com.mb.modulith.clinic.base;

import com.mb.modulith.clinic.api.request.ApiAppointmentRequest;
import com.mb.modulith.clinic.api.request.ApiDoctorRequest;
import com.mb.modulith.clinic.api.request.ApiPatientRequest;
import com.mb.modulith.clinic.api.response.ApiAppointmentResponse;
import com.mb.modulith.clinic.api.response.ApiDoctorResponse;
import com.mb.modulith.clinic.api.response.ApiPatientResponse;
import com.mb.modulith.clinic.data.entity.Appointment;
import com.mb.modulith.clinic.data.entity.Doctor;
import com.mb.modulith.clinic.data.entity.Patient;
import com.mb.modulith.clinic.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public abstract class BaseUnitTest {

    public Appointment getAppointment() {
        Appointment appointment = new Appointment();
        appointment.setRefundedFee(BigDecimal.ONE);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new Doctor(1, "Hank", "Mark", BigDecimal.ONE, false));
        appointment.setPatient(new Patient(1, "Hank", "Mark", false));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(3));
        appointment.setStartDate(OffsetDateTime.now());
        return appointment;
    }

    public ApiAppointmentRequest getApiAppointmentRequest() {
        ApiAppointmentRequest apiAppointmentRequest = new ApiAppointmentRequest();
        apiAppointmentRequest.setDoctorId(1);
        apiAppointmentRequest.setPatientId(1);
        apiAppointmentRequest.setEndDate(OffsetDateTime.now().plusHours(3));
        apiAppointmentRequest.setStartDate(OffsetDateTime.now());
        return apiAppointmentRequest;
    }

    public ApiAppointmentResponse getApiAppointmentResponse() {
        ApiAppointmentResponse appointment = new ApiAppointmentResponse();
        appointment.setRefundedFee(BigDecimal.ONE);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new ApiDoctorResponse(1, "Hank", "Mark", BigDecimal.ONE));
        appointment.setPatient(new ApiPatientResponse(1, "Justin", "Mark"));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(3));
        appointment.setStartDate(OffsetDateTime.now());
        appointment.setStatus(AppointmentStatus.ACTIVE);
        return appointment;
    }

    public List<Appointment> getAllAppointments() {
        Appointment appointment = new Appointment();
        appointment.setRefundedFee(BigDecimal.ONE);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new Doctor(1, "Hank", "Mark", BigDecimal.ONE, false));
        appointment.setPatient(new Patient(1, "Hank", "Mark", false));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(3));
        appointment.setStartDate(OffsetDateTime.now());

        Appointment appointment2 = new Appointment();
        appointment.setRefundedFee(BigDecimal.TEN);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new Doctor(1, "Mark", "Jack", BigDecimal.ONE, false));
        appointment.setPatient(new Patient(1, "Steve", "Mark", false));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(4));
        appointment.setStartDate(OffsetDateTime.now());
        return Arrays.asList(appointment, appointment2);
    }

    public List<ApiAppointmentResponse> getAllApiAppointmentResponses() {
        ApiAppointmentResponse appointment = new ApiAppointmentResponse();
        appointment.setRefundedFee(BigDecimal.ONE);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new ApiDoctorResponse(1, "Hank", "Mark", BigDecimal.ONE));
        appointment.setPatient(new ApiPatientResponse(1, "Hank", "Mark"));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(3));
        appointment.setStartDate(OffsetDateTime.now());

        ApiAppointmentResponse appointment2 = new ApiAppointmentResponse();
        appointment.setRefundedFee(BigDecimal.TEN);
        appointment.setFee(BigDecimal.TEN);
        appointment.setDoctor(new ApiDoctorResponse(1, "Mark", "Jack", BigDecimal.ONE));
        appointment.setPatient(new ApiPatientResponse(1, "Steve", "Mark"));
        appointment.setId(1);
        appointment.setEndDate(OffsetDateTime.now().plusHours(4));
        appointment.setStartDate(OffsetDateTime.now());
        return Arrays.asList(appointment, appointment2);
    }

    public List<Doctor> getDoctors() {
        return Arrays.asList(new Doctor(1, "Hank", "Mark", BigDecimal.ONE, false), new Doctor(1, "Hank", "Mark", BigDecimal.ONE, false));
    }

    public List<ApiDoctorResponse> getApiDoctorResponses() {
        return Arrays.asList(new ApiDoctorResponse(1, "Hank", "Mark", BigDecimal.ONE), new ApiDoctorResponse(1, "Hank", "Mark", BigDecimal.ONE));
    }

    public Doctor getDoctor() {
        return new Doctor(1, "Hank", "Mark", BigDecimal.ONE, false);
    }

    public ApiDoctorResponse getApiDoctorResponse() {
        return new ApiDoctorResponse(1, "Hank", "Mark", BigDecimal.ONE);
    }

    public ApiDoctorRequest getApiDoctorRequest() {
        return new ApiDoctorRequest("Hank", "Mark", BigDecimal.ONE);
    }

    public List<Patient> getPatients() {
        return Arrays.asList(new Patient(1, "Hank", "Mark", false), new Patient(1, "Hank", "Mark", false));
    }

    public List<ApiPatientResponse> getApiPatientResponses() {
        return Arrays.asList(new ApiPatientResponse(1, "Hank", "Mark"), new ApiPatientResponse(1, "Hank", "Mark"));
    }

    public Patient getPatient() {
        return new Patient(1, "Hank", "Mark", false);
    }

    public ApiPatientResponse getApiPatientResponse() {
        return new ApiPatientResponse(1, "Hank", "Mark");
    }

    public ApiPatientRequest getApiPatientRequest() {
        return new ApiPatientRequest(1, "Hank", "Mark");
    }

}
