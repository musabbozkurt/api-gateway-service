package com.mb.modulith.school.api.controller;

import com.mb.modulith.school.api.filter.ApiStudentFilter;
import com.mb.modulith.school.api.request.ApiStudentRequest;
import com.mb.modulith.school.api.response.ApiStudentResponse;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.mapping.StudentMapper;
import com.mb.modulith.school.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final StudentMapper studentMapper;

    /**
     * Add Student.
     *
     * @param apiStudentRequest to add student.
     */
    @PostMapping("/students")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiStudentResponse addStudent(@Valid @RequestBody ApiStudentRequest apiStudentRequest) {
        log.info("Received a request to add student. addStudent - ApiStudent {}", apiStudentRequest);
        return studentMapper.toResponseDto(studentService.addStudent(studentMapper.toEntity(apiStudentRequest)));
    }

    /**
     * Update student.
     *
     * @param apiStudentRequest to update student.
     */
    @PutMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiStudentResponse updateStudent(@PathVariable Long studentId, @Valid @RequestBody ApiStudentRequest apiStudentRequest) {
        log.info("Received a request to update student. updateStudent - studentId: {} ApiStudent {}", studentId, apiStudentRequest);
        return studentMapper.toResponseDto(studentService.updateStudent(studentId, studentMapper.toEntity(apiStudentRequest)));
    }

    /**
     * Get Students By Filter.
     *
     * @param apiStudentFilter to get students.
     */
    @GetMapping("/students")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<ApiStudentResponse> getStudents(ApiStudentFilter apiStudentFilter) {
        log.info("Received a request to get students. getStudents - ApiCourseFilter {}", apiStudentFilter);
        return studentMapper.toDto(studentService.getStudents(apiStudentFilter));
    }

    /**
     * Remove Student By studentId.
     *
     * @param studentId to remove student.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/students/{studentId}")
    public String removeStudent(@PathVariable Long studentId) {
        log.info("Received a request to remove student. removeStudent - courseId {}", studentId);
        studentService.removeStudent(studentId);
        return "Student with Id:" + studentId + " has been removed.";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/students-by-course-name/{courseName}")
    public Set<Student> getStudentsByCourseName(@PathVariable String courseName) {
        log.info("Received a request to get students by course name. getStudentsByCourseName - courseId {}", courseName);
        return studentService.getStudentsByCourseName(courseName);
    }

}
