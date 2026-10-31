package com.mb.modulith.school.mapping;

import com.mb.modulith.school.api.request.ApiStudentRequest;
import com.mb.modulith.school.api.response.ApiStudentResponse;
import com.mb.modulith.school.data.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    ApiStudentRequest toDto(Student student);

    Student toEntity(ApiStudentRequest apiStudentRequest);

    ApiStudentResponse toResponseDto(Student student);

    List<ApiStudentResponse> toDto(List<Student> entityList);

    Student toEntity(Student student);

}
