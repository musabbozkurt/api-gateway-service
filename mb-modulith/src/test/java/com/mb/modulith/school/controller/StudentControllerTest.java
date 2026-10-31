package com.mb.modulith.school.controller;

import com.mb.modulith.school.api.controller.StudentController;
import com.mb.modulith.school.api.request.ApiStudentRequest;
import com.mb.modulith.school.api.response.ApiStudentResponse;
import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.mapping.StudentMapper;
import com.mb.modulith.school.service.StudentService;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = StudentController.class)
@ContextConfiguration(classes = StudentController.class)
class StudentControllerTest extends BaseUnitTest {

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private StudentMapper studentMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addStudent_ShouldReturnResponse_WhenRequestIsValid() throws Exception {
        // Arrange
        Student student = getStudent();
        ApiStudentResponse response = ApiStudentResponse.builder().studentId(student.getStudentId()).build();
        when(studentMapper.toEntity(any(ApiStudentRequest.class))).thenReturn(student);
        when(studentService.addStudent(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponseDto(any(Student.class))).thenReturn(response);

        // Act
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getApiStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(student.getStudentId()));
    }

    @Test
    void getStudentsByCourseName_ShouldReturnStudents_WhenCourseHasEnrollments() throws Exception {
        // Arrange
        String courseName = getCourse().getCourseName();
        Set<Student> students = new HashSet<>();
        students.add(getStudent());
        when(studentService.getStudentsByCourseName(courseName)).thenReturn(students);

        // Act
        mockMvc.perform(get("/students-by-course-name/{courseName}", courseName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
