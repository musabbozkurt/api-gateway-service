package com.mb.modulith.school.mapping;

import com.mb.modulith.school.api.request.ApiCourseRequest;
import com.mb.modulith.school.api.response.ApiCourseResponse;
import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.data.model.Course;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseMapperTest extends BaseUnitTest {

    private final CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @Test
    void toEntity_ShouldMapRequest_WhenRequestIsValid() {
        // Arrange
        ApiCourseRequest request = getApiCourse();

        // Act
        Course result = courseMapper.toEntity(request);

        // Assertions
        assertEquals(request.getCourseName(), result.getCourseName());
        assertEquals(request.getCourseDuration(), result.getCourseDuration());
    }

    @Test
    void toResponseDto_ShouldMapCourse_WhenCourseIsValid() {
        // Arrange
        Course course = getCourse();

        // Act
        ApiCourseResponse result = courseMapper.toResponseDto(course);

        // Assertions
        assertEquals(course.getCourseId(), result.getCourseId());
        assertEquals(course.getCourseName(), result.getCourseName());
    }

    @Test
    void toDto_ShouldMapCourseToRequest_WhenCourseIsValid() {
        // Arrange
        Course course = getCourse();

        // Act
        ApiCourseRequest result = courseMapper.toDto(course);

        // Assertions
        assertEquals(course.getCourseName(), result.getCourseName());
    }

    @Test
    void toDto_ShouldMapCourseList_WhenListIsNotEmpty() {
        // Arrange
        List<Course> courses = List.of(getCourse());

        // Act
        List<ApiCourseResponse> result = courseMapper.toDto(courses);

        // Assertions
        assertEquals(1, result.size());
    }
}
