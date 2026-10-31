package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiDoctorRequest;
import com.mb.modulith.clinic.api.response.ApiDoctorResponse;
import com.mb.modulith.clinic.data.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorMapper {

    ApiDoctorResponse map(Doctor doctor);

    List<ApiDoctorResponse> map(List<Doctor> doctors);

    Doctor map(ApiDoctorRequest apiDoctorRequest);

}
