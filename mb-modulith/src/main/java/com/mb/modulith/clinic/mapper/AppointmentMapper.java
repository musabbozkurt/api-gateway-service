package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiAppointmentRequest;
import com.mb.modulith.clinic.api.response.ApiAppointmentResponse;
import com.mb.modulith.clinic.data.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    ApiAppointmentResponse map(Appointment appointment);

    List<ApiAppointmentResponse> map(List<Appointment> appointments);

    Appointment map(ApiAppointmentRequest apiAppointmentRequest);

}
