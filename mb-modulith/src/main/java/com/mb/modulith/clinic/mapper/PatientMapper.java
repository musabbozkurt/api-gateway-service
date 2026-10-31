package com.mb.modulith.clinic.mapper;

import com.mb.modulith.clinic.api.request.ApiPatientRequest;
import com.mb.modulith.clinic.api.response.ApiPatientResponse;
import com.mb.modulith.clinic.data.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    ApiPatientResponse map(Patient patient);

    List<ApiPatientResponse> map(List<Patient> patients);

    Patient map(ApiPatientRequest apiPatientRequest);
}
