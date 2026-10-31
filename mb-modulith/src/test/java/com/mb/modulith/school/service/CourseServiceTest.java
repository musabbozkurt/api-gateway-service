package com.mb.modulith.school.service;

import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.config.LimitsConfigProperties;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.data.repository.CourseRepository;
import com.mb.modulith.school.exception.SchoolRegistrationException;
import com.mb.modulith.school.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest extends BaseUnitTest {

    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LimitsConfigProperties configProperties;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl(courseRepository, configProperties);
    }

    @Test
    void addCourse_ShouldPersistCourse_WhenCourseIsValid() {
        // Arrange
        Course course = getCourse();
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        courseService.addCourse(course);

        // Assertions
        assertEquals(2, course.getCourseId().longValue());
    }

    @Test
    void removeCourse_ShouldDeleteCourse_WhenCourseExists() {
        // Arrange
        Course course = getCourse();
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // Act
        courseService.removeCourse(course.getCourseId());

        // Assertions
        verify(courseRepository, times(1)).delete(course);
    }

    @Test
    void removeCourse_ShouldThrowException_WhenCourseNotFound() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        SchoolRegistrationException exception = assertThrows(SchoolRegistrationException.class, () -> courseService.removeCourse(courseId));

        // Assertions
        assertEquals(SchoolRegistrationException.class, exception.getClass());
    }

    @Test
    void registerStudentToCourse_ShouldUpdateCourse_WhenCourseExists() {
        // Arrange
        Course course = getCourse();
        Long courseId = course.getCourseId();
        course.setStudents(Collections.emptySet());
        Set<Student> students = Collections.emptySet();
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        int maximumNumberOfStudents = 50;
        when(configProperties.getMaximumNumberOfStudents()).thenReturn(maximumNumberOfStudents);

        // Act
        courseService.registerStudentToCourse(courseId, students);

        // Assertions
        assertEquals(courseId, course.getCourseId());
        assertEquals(0, course.getStudents().size());
    }

    @Test
    void registerStudentToCourse_ShouldThrowException_WhenCourseNotFound() {
        // Arrange
        Long courseId = getCourse().getCourseId();
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        Set<Student> students = Collections.emptySet();

        // Act
        SchoolRegistrationException exception = assertThrows(SchoolRegistrationException.class, () -> courseService.registerStudentToCourse(courseId, students));

        // Assertions
        assertEquals(SchoolRegistrationException.class, exception.getClass());
    }

    @Test
    void getCourseByCourseName_ShouldReturnCourse_WhenCourseExists() {
        // Arrange
        Course course = getCourse();
        String courseName = course.getCourseName();
        when(courseRepository.findCourseByCourseName(courseName)).thenReturn(Optional.of(course));

        // Act
        Optional<Course> result = courseService.getCourseByCourseName(courseName);

        // Assertions
        assertEquals(courseName, result.orElseThrow().getCourseName());
    }
}
