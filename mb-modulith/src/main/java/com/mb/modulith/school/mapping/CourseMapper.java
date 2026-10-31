package com.mb.modulith.school.mapping;

import com.mb.modulith.school.api.request.ApiCourseRequest;
import com.mb.modulith.school.api.response.ApiCourseResponse;
import com.mb.modulith.school.data.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    ApiCourseRequest toDto(Course course);

    Course toEntity(ApiCourseRequest apiCourseRequest);

    ApiCourseResponse toResponseDto(Course course);

    List<ApiCourseResponse> toDto(List<Course> entityList);

}
