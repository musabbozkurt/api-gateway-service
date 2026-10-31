package com.mb.modulith.school.controller;

import com.mb.modulith.school.api.controller.CourseController;
import com.mb.modulith.school.api.request.ApiCourseRequest;
import com.mb.modulith.school.api.response.ApiCourseResponse;
import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.mapping.CourseMapper;
import com.mb.modulith.school.service.CourseService;
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

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = CourseController.class)
@ContextConfiguration(classes = CourseController.class)
class CourseControllerTest extends BaseUnitTest {

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private CourseMapper courseMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addCourse_ShouldReturnResponse_WhenRequestIsValid() throws Exception {
        // Arrange
        Course course = getCourse();
        ApiCourseResponse response = ApiCourseResponse.builder().courseId(course.getCourseId()).build();
        when(courseMapper.toEntity(any(ApiCourseRequest.class))).thenReturn(course);
        when(courseService.addCourse(any(Course.class))).thenReturn(course);
        when(courseMapper.toResponseDto(any(Course.class))).thenReturn(response);

        // Act
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getApiCourse())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(course.getCourseId()));
    }

    @Test
    void enrollStudentToCourse_ShouldReturnResponse_WhenEnrollmentSucceeds() throws Exception {
        // Arrange
        Course course = getCourse();
        Long courseId = course.getCourseId();
        Set<Student> students = Collections.emptySet();
        ApiCourseResponse response = ApiCourseResponse.builder().courseId(courseId).build();
        when(courseMapper.toResponseDto(course)).thenReturn(response);
        when(courseService.registerStudentToCourse(courseId, students)).thenReturn(course);

        // Act
        mockMvc.perform(put("/students-to-course/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(students)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId));
    }
}
