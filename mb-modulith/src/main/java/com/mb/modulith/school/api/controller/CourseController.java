package com.mb.modulith.school.api.controller;

import com.mb.modulith.school.api.filter.ApiCourseFilter;
import com.mb.modulith.school.api.request.ApiCourseRequest;
import com.mb.modulith.school.api.response.ApiCourseResponse;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.mapping.CourseMapper;
import com.mb.modulith.school.service.CourseService;
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
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    /**
     * Add Course.
     *
     * @param apiCourseRequest to add course.
     */
    @PostMapping("/courses")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiCourseResponse addCourse(@Valid @RequestBody ApiCourseRequest apiCourseRequest) {
        log.info("Received a request to add course. addCourse - ApiCourse {}", apiCourseRequest);
        return courseMapper.toResponseDto(courseService.addCourse(courseMapper.toEntity(apiCourseRequest)));
    }

    /**
     * Update Course.
     *
     * @param apiCourseRequest to update course.
     */
    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiCourseResponse updateCourse(@PathVariable Long courseId, @Valid @RequestBody ApiCourseRequest apiCourseRequest) {
        log.info("Received a request to update course. updateCourse - Course {}", apiCourseRequest);
        return courseMapper.toResponseDto(courseService.updateCourse(courseId, courseMapper.toEntity(apiCourseRequest)));
    }

    /**
     * Get Courses By Filter.
     *
     * @param apiCourseFilter to get courses.
     */
    @GetMapping("/courses")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<ApiCourseResponse> getCourses(ApiCourseFilter apiCourseFilter) {
        log.info("Received a request to get courses. getCourses - ApiCourseFilter {}", apiCourseFilter);
        return courseMapper.toDto(courseService.getCourses(apiCourseFilter));
    }

    /**
     * Remove Course By courseId.
     *
     * @param courseId to remove course.
     */
    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String removeCourse(@PathVariable Long courseId) {
        log.info("Received a request to remove course. removeCourse - courseId {}", courseId);
        courseService.removeCourse(courseId);
        return "Course with Id:" + courseId + " has been removed.";
    }

    @PutMapping("/students-to-course/{courseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ApiCourseResponse enrollStudentToCourse(@PathVariable Long courseId, @RequestBody Set<Student> students) {
        log.info("Received a request to enroll student to course. enrollStudentToCourse - courseId {}", courseId);
        return courseMapper.toResponseDto(courseService.registerStudentToCourse(courseId, students));
    }

}
