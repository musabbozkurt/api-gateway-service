package com.mb.modulith.school.mapping;

import com.mb.modulith.school.api.request.ApiStudentRequest;
import com.mb.modulith.school.api.response.ApiStudentResponse;
import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.data.model.Student;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentMapperTest extends BaseUnitTest {

    private final StudentMapper studentMapper = Mappers.getMapper(StudentMapper.class);

    @Test
    void toEntity_ShouldMapRequest_WhenRequestIsValid() {
        // Arrange
        ApiStudentRequest request = getApiStudent();

        // Act
        Student result = studentMapper.toEntity(request);

        // Assertions
        assertEquals(request.getStudentName(), result.getStudentName());
        assertEquals(request.getMobileNumber(), result.getMobileNumber());
    }

    @Test
    void toResponseDto_ShouldMapStudent_WhenStudentIsValid() {
        // Arrange
        Student student = getStudent();

        // Act
        ApiStudentResponse result = studentMapper.toResponseDto(student);

        // Assertions
        assertEquals(student.getStudentId(), result.getStudentId());
        assertEquals(student.getStudentName(), result.getStudentName());
    }

    @Test
    void toDto_ShouldMapStudentToRequest_WhenStudentIsValid() {
        // Arrange
        Student student = getStudent();

        // Act
        ApiStudentRequest result = studentMapper.toDto(student);

        // Assertions
        assertEquals(student.getStudentName(), result.getStudentName());
    }

    @Test
    void toDto_ShouldMapStudentList_WhenListIsNotEmpty() {
        // Arrange
        List<Student> students = List.of(getStudent());

        // Act
        List<ApiStudentResponse> result = studentMapper.toDto(students);

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void toEntity_ShouldCopyStudent_WhenSourceIsProvided() {
        // Arrange
        Student source = getStudent();

        // Act
        Student result = studentMapper.toEntity(source);

        // Assertions
        assertEquals(source.getStudentName(), result.getStudentName());
        assertEquals(source.getAddress(), result.getAddress());
    }
}
